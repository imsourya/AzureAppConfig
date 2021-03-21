package com.ey;

import java.util.Optional;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.util.Context;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
	final static String CONN_STR = "Endpoint=https://eympapp.azconfig.io;Id=ZwWj-l0-s0:WSWK2Lt8n6CGwp9mklMw;Secret=bJonic2D6pYeS+ERZS6KC/qpNbzMwPeggnrYN9z4wCk=";
	
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");
        
        String connectionString = "";
        try {
        	connectionString = System.getenv("APP_CONF_CONNECTION_STRING");
        } catch(Exception e){
        	connectionString = CONN_STR;
        }

        //////////////// App Configuration //////////////
        final String national = request.getQueryParameters().get("national");
        
        ConfigurationClient configurationClient = new ConfigurationClientBuilder()
        	    .connectionString(connectionString)
        	    .buildClient();
        ConfigurationSetting confSet = null;
        try {
        	confSet = configurationClient.getConfigurationSetting(national, "");

        	System.out.println("======>"+confSet.getKey() +":"+confSet.getValue());
        }catch(ResourceNotFoundException e) {
        	e.printStackTrace();
        	return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Sorry We can not find national "+national).build();
        }
        /////////////////////////////
        
        /////////////////////////////

        // Parse query parameter
        final String query = request.getQueryParameters().get("name");
        final String name = request.getBody().orElse(query);

        if (name == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + name+", Answer to national "+confSet.getKey()+" is "+confSet.getValue()).build();
        }
    }
}
