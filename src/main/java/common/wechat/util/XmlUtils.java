package common.wechat.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author LongerHsiao
 * @date 2017-12-5
 */
public class XmlUtils {
    private static Logger logger = LoggerFactory.getLogger(XmlUtils.class);

    public static String toXml(Object obj) {
        String result = null;
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            JAXBContext context = JAXBContext.newInstance(obj.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);
            marshaller.marshal(obj, outputStream);
            result = outputStream.toString("utf-8");
        } catch (Exception e) {
            logger.error("<<======XmlUtils.toXml:", e);
        }
        return result;
    }

    public static Map<String, String> xmlToMap(InputStream is)
            throws ParserConfigurationException, SAXException, IOException {
        Map<String, String> map = new HashMap<>();
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(is);
        Element root = document.getDocumentElement();
        NodeList list = root.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            map.put(node.getNodeName(), node.getTextContent());
        }
        return map;
    }
}
