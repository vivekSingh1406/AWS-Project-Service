package com.example.AmazonS3RDSService.xml;

import com.example.AmazonS3RDSService.config.XmlProcessingProperties;
import com.example.AmazonS3RDSService.exception.ProcessingException;
import com.example.AmazonS3RDSService.exception.ValidationDetail;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class SecureXmlProcessor {
    private static final String XBRL_NAMESPACE = "http://www.xbrl.org/2003/instance";
    private static final String BUSINESS_NAMESPACE = "https://www.sebi.gov.in/xbrl/2025-03-31/in-capmkt";
    private static final Set<String> REQUIRED_FACTS = Set.of(
            "NameOfTheCompany", "ISIN", "DateOfReport", "TypeOfMeeting", "TypeOfIntimation");

    private final XmlProcessingProperties properties;

    public SecureXmlProcessor(XmlProcessingProperties properties) {
        this.properties = properties;
    }

    public Map<String, Object> parse(byte[] xml) {
        if (xml == null || xml.length == 0) {
            throw new ProcessingException("MALFORMED_XML", "XML document is empty");
        }

        BusinessFactHandler handler = new BusinessFactHandler(properties);
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            var reader = factory.newSAXParser().getXMLReader();
            reader.setEntityResolver((publicId, systemId) -> {
                throw new SAXException("External entities are prohibited");
            });
            reader.setContentHandler(handler);
            reader.parse(new InputSource(new ByteArrayInputStream(xml)));
            return handler.validatedFacts();
        } catch (LimitSaxException exception) {
            throw new ProcessingException("XML_LIMIT_EXCEEDED", "XML document exceeds configured complexity limits");
        } catch (SAXParseException exception) {
            String message = safeMessage(exception);
            String code = message.contains("doctype") || message.contains("external entit")
                    ? "XML_SECURITY_VIOLATION" : "MALFORMED_XML";
            throw new ProcessingException(code, code.equals("XML_SECURITY_VIOLATION")
                    ? "Prohibited XML construct detected" : "XML document is not well formed");
        } catch (ProcessingException exception) {
            throw exception;
        } catch (Exception exception) {
            String message = safeMessage(exception);
            if (message.contains("doctype") || message.contains("external entit")) {
                throw new ProcessingException("XML_SECURITY_VIOLATION", "Prohibited XML construct detected");
            }
            throw new ProcessingException("MALFORMED_XML", "XML document could not be parsed", exception);
        }
    }

    private String safeMessage(Throwable failure) {
        return String.valueOf(failure.getMessage()).toLowerCase(Locale.ROOT);
    }

    private static final class BusinessFactHandler extends DefaultHandler {
        private final XmlProcessingProperties limits;
        private final Map<String, Object> facts = new LinkedHashMap<>();
        private int depth;
        private int elements;
        private long totalCharacters;
        private int currentElementCharacters;
        private int factDepth = -1;
        private String factName;
        private StringBuilder factValue;
        private boolean xbrlRoot;

        private BusinessFactHandler(XmlProcessingProperties limits) {
            this.limits = limits;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes)
                throws SAXException {
            depth++;
            elements++;
            currentElementCharacters = 0;
            if (depth > limits.maxDepth() || elements > limits.maxElements()) {
                throw new LimitSaxException();
            }
            if (depth == 1) {
                xbrlRoot = XBRL_NAMESPACE.equals(uri) && "xbrl".equals(localName);
            }
            if (BUSINESS_NAMESPACE.equals(uri) && factDepth == -1) {
                factDepth = depth;
                factName = localName;
                factValue = new StringBuilder();
            }
        }

        @Override
        public void characters(char[] chars, int start, int length) throws SAXException {
            totalCharacters += length;
            currentElementCharacters += length;
            if (totalCharacters > limits.maxTotalCharacters()
                    || currentElementCharacters > limits.maxElementCharacters()) {
                throw new LimitSaxException();
            }
            if (factDepth != -1) {
                factValue.append(chars, start, length);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (depth == factDepth) {
                addFact(factName, normalize(factValue.toString()));
                factDepth = -1;
                factName = null;
                factValue = null;
            }
            depth--;
            currentElementCharacters = 0;
        }

        private Map<String, Object> validatedFacts() {
            if (!xbrlRoot) {
                throw new ProcessingException("INVALID_XBRL", "The uploaded XML is not an XBRL document");
            }
            if (facts.isEmpty()) {
                throw new ProcessingException("INVALID_XBRL", "No SEBI business elements were found");
            }

            List<ValidationDetail> errors = REQUIRED_FACTS.stream()
                    .filter(name -> !facts.containsKey(name) || String.valueOf(facts.get(name)).isBlank())
                    .sorted()
                    .map(name -> ValidationDetail.field(name, "Required XML element is missing or empty"))
                    .toList();
            if (!errors.isEmpty()) {
                throw new ProcessingException("XBRL_VALIDATION_FAILED",
                        "Required XBRL business elements are missing or invalid", errors);
            }
            return Collections.unmodifiableMap(new LinkedHashMap<>(facts));
        }

        private void addFact(String name, String value) {
            Object existing = facts.get(name);
            if (existing == null) {
                facts.put(name, value);
            } else if (existing instanceof List<?> values) {
                @SuppressWarnings("unchecked")
                List<String> strings = (List<String>) values;
                strings.add(value);
            } else {
                List<String> values = new ArrayList<>();
                values.add(String.valueOf(existing));
                values.add(value);
                facts.put(name, values);
            }
        }

        private String normalize(String value) {
            return value.replaceAll("\\s+", " ").trim();
        }
    }

    private static final class LimitSaxException extends SAXException {
        private LimitSaxException() {
            super("XML limit exceeded");
        }
    }
}
