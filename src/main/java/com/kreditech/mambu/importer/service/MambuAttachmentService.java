package com.kreditech.mambu.importer.service;

import com.kreditech.mambu.importer.rest.RestClient;
import com.kreditech.mambu.importer.model.MambuAttachment;
import org.apache.commons.codec.binary.Base64;

import javax.json.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetching mambu attachments for client and loan
 */
public class MambuAttachmentService {

    private RestClient restClient;

    public MambuAttachmentService(String mambuUrl, String username, String password) {
        this.restClient = new RestClient(mambuUrl, username, password);
    }

    public String getClientKeyForLoan(String loanIdentifier) {
        String loanMetadata = restClient.getDataFromServer("api/loans/" + loanIdentifier);
        JsonReader jsonReader = Json.createReader(new StringReader(loanMetadata));
        JsonObject loanDetailsObject = jsonReader.readObject();
        jsonReader.close();

        return loanDetailsObject.getString("accountHolderKey");
    }

    public List<MambuAttachment> getDocumentsForClient(String clientKey, int limitAttachmentsPerEntity) {
        String documentsMetadata = restClient.getDataFromServer("api/clients/" + clientKey + "/documents/");

        JsonReader jsonReader = Json.createReader(new StringReader(documentsMetadata));
        JsonArray documentListObject = jsonReader.readArray();
        jsonReader.close();

        System.out.println("Found: " + documentListObject.size() + " documents.");

        List<MambuAttachment> documents = new ArrayList<>();
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

            if (documents.size() == limitAttachmentsPerEntity)
                break;
        }
        return documents;

    }

    /**
    Get document binary content. Mambu stores documents on S3 and only metadata are stored in Database.
     */
    public byte[] getDocumentContent(int documentIdentifier) {
        String content = restClient.getDataFromServer("/api/documents/" + documentIdentifier);
        return Base64.decodeBase64(content.getBytes());
    }
}
