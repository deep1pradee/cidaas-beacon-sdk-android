package cidaasbeaconsdk;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import i.widaas.cidaaasbeaconsdk.R;

public class SDKEntity {
    String baseUrl;
    public static SDKEntity SDKEntityInstance = new SDKEntity();
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void readInputs(AssetManager assetManager, String fileName, Context context) {
        InputStream inputStream = null;
        try {

            if (assetManager == null || fileName == "" || context == null) {
                return;
            }
            inputStream = assetManager.open(fileName);
        } catch (IOException e) {
            Log.e("tag", e.getMessage());
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
            Document propertiesXML = obtenerDocumentDeByte(outputStream.toByteArray());
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("//resources/item[@string]");
            NodeList nl = (NodeList) expr.evaluate(propertiesXML, XPathConstants.NODESET);
            NodeList nodeList = propertiesXML.getElementsByTagName("item");
            for (int x = 0, size = nodeList.getLength(); x < size; x++) {
                if (nodeList.item(x).getAttributes().getNamedItem("name").getNodeValue().
                        equalsIgnoreCase(context.getApplicationContext().getString(R.string.baseURL))) {
                    baseUrl = nodeList.item(x).getTextContent().trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Document obtenerDocumentDeByte(byte[] documentoXml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {

            byte[] Docfile=documentoXml;
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new ByteArrayInputStream(documentoXml));
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
