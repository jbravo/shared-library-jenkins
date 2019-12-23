import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonSlurperClassic

public class RestClient {

	private String baseUrl;
	private String token;


	def call(baseUrl, token) {
		println("TESTEEEE")

		this.baseUrl = baseUrl;
		this.token = token;
	}

	def getIdProject(String namespace) {
		Map resultMap = get(this.baseUrl.concat("/api/v4/projects/").concat(namespace))
		return resultMap.get("id")
	}

	def createMR(Integer idProject, String sourceBranch, String targetBranch) {
		Map<String, String> params = new HashMap();
		params.put("source_branch", sourceBranch);
		params.put("target_branch", targetBranch);
		params.put("title", "Teste MR");

		String uri = this.baseUrl.concat("/api/v4/projects/").concat(idProject.toString()).concat("merge_requests")

		return post(uri, params)
	}

	@NonCPS
	def get(String uri) {
		HttpURLConnection connection = new URL(uri).openConnection()
		connection.setRequestProperty("Private-Token", this.token)
		connection.setRequestMethod("GET")
		connection.setDoInput(true)
		connection.setDoOutput(true)
		def rs = null
		try {
			connection.connect()
			def responseCode = connection.getResponseCode()
			println "Response Code: "+responseCode
			rs = new JsonSlurperClassic().parse(new InputStreamReader(connection.getInputStream(), "UTF-8"))
		} finally {
			connection.disconnect()
		}
		return rs
	}

	@NonCPS
	def post(String uri, Map params) {
		println "Executando POST: " + uri

		String response = "";
		try {
			HttpURLConnection connection = new URL(uri).openConnection();
			connection.setReadTimeout(15000);
			connection.setConnectTimeout(15000);
			connection.setRequestMethod("POST");
			connection.setDoInput(true);
			connection.setDoOutput(true);


			OutputStream os = connection.getOutputStream();
			BufferedWriter writer = new BufferedWriter(
					new OutputStreamWriter(os, "UTF-8"));

			writer.write(getPostDataString(params));

			writer.flush();
			writer.close();
			os.close();
			int responseCode = connection.getResponseCode();

			println "Response Code: " + responseCode

			if (responseCode == HttpURLConnection.HTTP_OK) {
				String line;
				BufferedReader br=new BufferedReader(new InputStreamReader(connection.getInputStream()));
				while ((line=br.readLine()) != null) {
					response+=line;
				}
			}
			else {
				response="";
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return response;
	}

	@NonCPS
	def getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException{
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for(Map.Entry<String, String> entry : params.entrySet()){
			if (first)
				first = false;
			else
				result.append("&");

			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
		}

		return result.toString();
	}
}