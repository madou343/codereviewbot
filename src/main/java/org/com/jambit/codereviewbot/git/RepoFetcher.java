package org.com.jambit.codereviewbot.git;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;


public class RepoFetcher {

    private String cloneFilepath;

    public void fetchRepo(String repoUrl, String filepathClone) throws GitAPIException {

        this.cloneFilepath = filepathClone;
        Git git = Git.cloneRepository()
                .setURI(repoUrl)
                .setBranch("master")
                .setDirectory(new java.io.File(filepathClone))
                .call();
        System.out.println("Cloned: " + git.getRepository().getDirectory());
    }

    public String getCloneFilepath() {
        return cloneFilepath;
    }
}
