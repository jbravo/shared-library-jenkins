package br.gov.mds

import com.github.zafarkhaja.semver.Version

public class GitFlow {

	private static final String BASE_URL = "http://sugitpd02.mds.net"
	private static final String PRIVATE_TOKEN = "u3xBWdP3KUxxG7PQYm_t"

	public Integer getIdProject(String namespace) {
		String uri = new StringBuilder(this.BASE_URL)
				.append("/api/v4/projects/").append(namespace).toString()
		Map resultMap = (Map) GitRestClient.get(uri, this.token)
		return resultMap.get("id")
	}

	public String createMR(Integer idProject, String sourceBranch) {
		Map<String, String> params = new HashMap();
		params.put("remove_source_branch", "true");
		params.put("source_branch", sourceBranch);
		params.put("target_branch", "develop");
		params.put("title", "Merge Request da branch: " + sourceBranch);

		String uri = new StringBuilder(this.BASE_URL)
				.append("/api/v4/projects/").append(idProject).append("/merge_requests").toString();

		return GitRestClient.post(uri, this.token, params)
	}

	public List<String> getFeatures(Integer idProject){
		String uri = new StringBuilder(this.BASE_URL)
				.append("/api/v4/projects/").append(idProject).append("/repository/branches").toString()
		List branches = GitRestClient.get(uri, this.token)

		List<String> features = new ArrayList();

		for (branch in branches) {
			String nomeBranch = branch.getAt("name");
			if(nomeBranch.startsWith("feature/")) {
				features.add(nomeBranch);
			}
		}
		return features;
	}

	public String getNextVersion(Integer idProject, SemVerTypeEnum semVerTypeEnum) {
		def ultimaTag = this.getUltimaTag(idProject)
		Version version = Version.valueOf(ultimaTag);

		if(SemVerTypeEnum.MAJOR.equals(semVerTypeEnum)) {
			return version.incrementMajorVersion()
		} else if(SemVerTypeEnum.MINOR.equals(semVerTypeEnum)) {
			return version.incrementMinorVersion()
		} else if(SemVerTypeEnum.PATCH.equals(semVerTypeEnum)) {
			return version.incrementPatchVersion()
		}
		return version.incrementPreReleaseVersion();
	}

	private String getUltimaTag(Integer idProject){
		String uri = new StringBuilder(this.BASE_URL)
				.append("/api/v4/projects/").append(idProject).append("/repository/tags").toString()

//		Map<String, String> params = new HashMap();
//		params.put("order_by", "name");

		List tags = GitRestClient.get(uri, this.token)

		if(!tags.empty) {
			Map tag = tags.get(0);
			return tag.get("name");
		}
		return "0.0.0"
	}


	public static void main(String[] args) {
		def baseUrl = 'http://sugitpd02.mds.net'
		def privateToken = 'u3xBWdP3KUxxG7PQYm_t'
		GitFlow flow = new GitFlow(baseUrl, privateToken)
		def tag = flow.getNextVersion(559, SemVerTypeEnum.PATCH);
		System.out.println(tag);
	}
}
