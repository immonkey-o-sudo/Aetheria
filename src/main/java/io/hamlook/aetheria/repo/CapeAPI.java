package io.hamlook.aetheria.repo;

import io.hamlook.aetheria.repo.data.RepoData;

public class CapeAPI {

    public static String getAPIUrl(){
        RepoData url = RepoHandler.get(ATHRRepo.KEY_REPO, RepoData.class,new RepoData());
        return url.capeApi;
    }

    public static String getAPIUrl(String endpoint){
        RepoData url = RepoHandler.get(ATHRRepo.KEY_REPO, RepoData.class,new RepoData());
        return url.capeApi + (url.capeApi.endsWith("/") || endpoint.startsWith("/") ? "" : "/") + endpoint;
    }

}
