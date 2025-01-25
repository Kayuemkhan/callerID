package com.chromatics.caller_id.common;

import com.chromatics.caller_id.Storage;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SiaMetadata {

    public interface SourceLocator {
        boolean useInternal();
    }

    public static class Country {
        public final String code;
        public final String url;

        Country(String code, String url) {
            this.code = code;
            this.url = url;
        }
    }

    public static final Country FALLBACK_COUNTRY = new Country("WW", "https://www.shouldianswer.net");

    private static final Logger LOG = LoggerFactory.getLogger(SiaMetadata.class);

    private static final int FALLBACK_APP_VERSION = 187;
    private static final String FALLBACK_OKHTTP_VERSION = "3.10.0";

    private final Storage storage;
    private final String pathPrefix;
    private final SourceLocator sourceLocator;

    private int siaAppVersion;
    private String siaOkHttpVersion;

    private final Map<String, Country> siaCountries = new HashMap<>();

    private boolean loaded;

    public SiaMetadata(Storage storage, String pathPrefix, SourceLocator sourceLocator) {
        this.storage = storage;
        this.pathPrefix = pathPrefix;
        this.sourceLocator = sourceLocator;
    }

    public int getSiaAppVersion() {
        checkLoaded();
        return siaAppVersion;
    }

    public String getSiaOkHttpVersion() {
        checkLoaded();
        return siaOkHttpVersion;
    }

    public Country getCountry(String code) {
        checkLoaded();

        Country country = siaCountries.get(code);
        if (country == null) country = FALLBACK_COUNTRY;
        return country;
    }

    public void reload() {
        load();
    }

    private void checkLoaded() {
        if (loaded) return;

        LOG.debug("checkLoaded() loading metadata");
        load();
    }

    private void reset() {
        loaded = false;

        siaAppVersion = FALLBACK_APP_VERSION;
        siaOkHttpVersion = FALLBACK_OKHTTP_VERSION;

        siaCountries.clear();
    }

    private void load() {
        LOG.debug("load() loading metadata");

        reset();

        load(sourceLocator.useInternal());
        loaded = true;
    }

    private void load(boolean useInternal) {
        LOG.debug("load() started; useInternal={}", useInternal);

        try (InputStream is = storage.openFile(pathPrefix + "sia_info.dat", useInternal);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {

            if (!"SIA".equals(bufferedReader.readLine())) {
                throw new RuntimeException("Incorrect extra info header");
            }

            String appVersionString = bufferedReader.readLine();
            siaAppVersion = Integer.parseInt(appVersionString);
            LOG.debug("load() siaAppVersion={}", siaAppVersion);

            siaOkHttpVersion = Objects.requireNonNull(bufferedReader.readLine());
            LOG.debug("load() okHttpVersion={}", siaOkHttpVersion);

            LOG.debug("load() loaded extra info");
        } catch (Exception e) {
            LOG.warn("load() failed to load extra info", e);
            LOG.debug("load() using siaAppVersion={}, okHttpVersion={}", siaAppVersion, siaOkHttpVersion);
        }

        LOG.debug("load() loading countries");
        try (InputStream is = storage.openFile(pathPrefix + "sia_countries.dat", useInternal);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is))) {

            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s).append('\n');
            }

            JSONArray countriesJsonArray = new JSONObject(sb.toString())
                    .getJSONArray("countries");

            for (int i = 0; i < countriesJsonArray.length(); i++) {
                JSONObject countryJson = countriesJsonArray.getJSONObject(i);

                String code = countryJson.getString("code");
                String url = countryJson.getString("url");

                siaCountries.put(code, new Country(code, url));
            }
        } catch (Exception e) {
            LOG.warn("load() failed to load countries", e);
        }
    }

}
