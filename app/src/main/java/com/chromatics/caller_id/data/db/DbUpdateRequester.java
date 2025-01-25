package com.chromatics.caller_id.data.db;

import android.util.Log;

import com.chromatics.caller_id.common.WebService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DbUpdateRequester {

    public static class Update {
        public enum Result {
            OK, NO_UPDATES, OUTDATED_APP, BAD_SECONDARY, UNEXPECTED_RESPONSE
        }

        private final ResponseBody body;
        private final Result result;

        public Update(ResponseBody body) {
            this(body, Result.OK);
        }

        public Update(Result result) {
            this(null, result);
        }

        private Update(ResponseBody body, Result result) {
            this.body = body;
            this.result = result;
        }

        public boolean hasUpdate() {
            return body != null;
        }

        public ResponseBody getBody() {
            return body;
        }

        public Result getResult() {
            return result;
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(DbUpdateRequester.class);

    private final WebService webService;

    public DbUpdateRequester(WebService webService) {
        this.webService = webService;
    }

    public Update getUpdate(int currentDatabaseVersion) throws IOException {
        Log.d("getUpdate({})", String.valueOf(currentDatabaseVersion));

        String urlPath = webService.getGetDatabaseUrlPart() + "/cached"
                + "?_dbVer=" + currentDatabaseVersion;

        Response response = webService.call(urlPath, new HashMap<>());

        ResponseBody body = Objects.requireNonNull(response.body());
        MediaType contentType = body.contentType();
        LOG.debug("getUpdate() response contentType={}", contentType);

        if (contentType != null && "application".equals(contentType.type())) {
            LOG.debug("getUpdate() got proper result");
            return new Update(body);
        } else {
            String responseString = body.string();
            LOG.debug("getUpdate() responseString={}", responseString);

            Update.Result result;
            switch (responseString.replaceAll("\n", "")) {
                case "OAP":
                    LOG.trace("getUpdate() server reported outdated app");
                    // outdated app
                    result = Update.Result.OUTDATED_APP;
                    break;

                case "NC":
                    LOG.trace("getUpdate() server reported no updates");
                    // "No checkAndUpdate available" - probably "up to date"
                    result = Update.Result.NO_UPDATES;
                    break;

                case "OOD":
                    LOG.trace("getUpdate() server suggests to reset secondary DB");
                    // remove secondary DB and retry
                    result = Update.Result.BAD_SECONDARY;
                    break;

                default:
                    LOG.warn("getUpdate() unknown response string: {}", responseString);
                    result = Update.Result.UNEXPECTED_RESPONSE;
                    break;
            }

            return new Update(result);
        }
    }

}
