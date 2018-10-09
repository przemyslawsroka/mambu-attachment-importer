package com.kreditech.mambu.importer;

import org.apache.commons.codec.binary.Base64;

import javax.json.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class MambuClient {

    private RestClient restClient;

    public MambuClient(String baseUrl, String username, String password) {
        this.restClient = new RestClient(baseUrl, username, password);
    }

    public String getClientKeyForLoan(String loanIdentifier) {
        String loanMetadata = restClient.getDataFromServer("api/loans/" + loanIdentifier);
        JsonReader jsonReader = Json.createReader(new StringReader(loanMetadata));
        JsonObject loanDetailsObject = jsonReader.readObject();
        jsonReader.close();

        String accountHolderKey = loanDetailsObject.getString("accountHolderKey");
        return accountHolderKey;
    }

    public List<MambuAttachment> getDocumentsForClient(String clientKey) {
        String documentsMetadata = restClient.getDataFromServer("api/clients/" + clientKey + "/documents/");

        JsonReader jsonReader = Json.createReader(new StringReader(documentsMetadata));
        JsonArray documentListObject = jsonReader.readArray();
        jsonReader.close();

        System.out.println("Found: " + documentListObject.size() + " documents.");

        List<MambuAttachment> documents = new ArrayList<MambuAttachment>();
        for (JsonValue listItem : documentListObject) {

            MambuAttachment document = new MambuAttachment();
            JsonObject jsonDocument = listItem.asJsonObject();
            document.setId(jsonDocument.getInt("id"));

            String fileName = jsonDocument.getString("name");
            String extension = jsonDocument.getString("type");
            document.setName(fileName + "." + extension);

            document.setLastModified(jsonDocument.getString("lastModifiedDate"));

            document.setClientKey(clientKey);
            documents.add(document);

            if (documents.size() == 3)
                break;
        }
        return documents;

    }

    public byte[] getDocumentContent(int documentIdentifier) {
        String content = restClient.getDataFromServer("/api/documents/" + documentIdentifier);
        byte[] image = Base64.decodeBase64(content.getBytes());
        return image;
    }
}
