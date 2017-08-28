package com.idugalic.documentation;

import com.structurizr.model.*;
import com.structurizr.view.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import com.structurizr.Workspace;
import com.structurizr.api.StructurizrClient;

/**
 * 
 * Software architecture diagrams with structurizr.com. 
 * 
 * Static diagrams, whether drawn on a whiteboard or with a general purpose diagramming tool such as Microsoft Visio, tend to get out of date quickly and often don't reflect the structure of the code. 
 * On the other hand, automatically generated diagrams, such as UML class diagrams created by reverse-engineering the code, typically show far too much detail, limiting their usefulness.
 *
 * @author idugalic
 */
@SpringBootApplication
public class DocumentationApplication {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentationApplication.class);
    
  
    private static final Long WORKSPACE_ID = 36994L;
    
    private static final String MONOLITH_TAG = "Modular Monolith";
    private static final String DATASTORE_TAG = "Database";
    
    /**
     * WORK IN PROGRESS (https://github.com/structurizr/java/blob/master/structurizr-examples/src/com/structurizr/example/core/MicroservicesExample.java)
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        SpringApplication app = new SpringApplication(DocumentationApplication.class);
        Environment env = app.run(args).getEnvironment();
       
        Workspace workspace = new Workspace("My Company - Monolithic", "An example of a modular monolithic architecture.");
        Model model = workspace.getModel();
        // ## System ##
        SoftwareSystem mySoftwareSystem = model.addSoftwareSystem("My Company Information System", "An enterprise application that enables users to manage blog posts, project information, customers and other data");
        mySoftwareSystem.setLocation(Location.Internal);
        Person user = model.addPerson("User", "A user");
        user.uses(mySoftwareSystem, "Uses");
        // ## UI  ##
        Container uiApplication = mySoftwareSystem.addContainer("UI Application", "A user interface that allows users to manage their profile, blogs and projects via web browser", "JavaScript, HTML, Angular4");
        uiApplication.setUrl("https://github.com/ivans-innovation-lab/my-company-angular-fe");
        user.uses(uiApplication, "Uses");
        // ## API  ##
        Container webApplication = mySoftwareSystem.addContainer("Web Application (REST API)", "A REST API that allows users to manage their profile, blogs and projects", "HTTP, Java, Spring Boot, Spring Data Rest");
        webApplication.setUrl("https://github.com/ivans-innovation-lab/my-company-monolith");
        webApplication.addTags(MONOLITH_TAG);
        uiApplication.uses(webApplication, "Consume");
        // ## DB ##
        Container database = mySoftwareSystem.addContainer("Database", "Stores all events (evensourcing), and materialized vies.", "Relational database");
        database.addTags(DATASTORE_TAG);
        webApplication.uses(database, "Store events and data projections", "SQL", InteractionStyle.Synchronous);

        Component webComponent = webApplication.addComponent("Web Component", "Exposes a REST API on top of command gateway and materialized views", "HTTP, Java, Spring Data Rest");
        user.uses(webComponent, "Uses");
        webComponent.setUrl("https://github.com/ivans-innovation-lab/my-company-monolith/tree/master/src/main/java/com/idugalic");

        Component projectCommandSideComponent = webApplication.addComponent("Project Command Side Component" , "Processes commands and persists and propagates Events", "Java, Spring, Axonframework");
        webComponent.uses(projectCommandSideComponent, "Send commands");
        projectCommandSideComponent.setUrl("https://github.com/ivans-innovation-lab/my-company-project-domain");
        projectCommandSideComponent.uses(database, "Event store");

        Component blogPostCommandSideComponent = webApplication.addComponent("BlogPost Command Side Component" , "Processes commands and persists and propagates Events", "Java, Spring, Axonframework");
        webComponent.uses(blogPostCommandSideComponent, "Send commands");
        blogPostCommandSideComponent.setUrl("https://github.com/ivans-innovation-lab/my-company-blog-domain");
        blogPostCommandSideComponent.uses(database, "Event store");

        Component projectQuerySideComponent = webApplication.addComponent("Project Query Side Component" , "Event-listener and processor. Builds and maintains a materialized view which tracks the state of the Project aggregate", "Java, Spring, Axonframework");
        webComponent.uses(projectQuerySideComponent, "Read materialized view");
        projectQuerySideComponent.uses(database, "Subscribes to event store and write materialized view");
        projectQuerySideComponent.setUrl("https://github.com/ivans-innovation-lab/my-company-project-materialized-view");

        Component blogPostQuerySideComponent = webApplication.addComponent("Blog Post Query Side Component" , "Event-listener and processor. Builds and maintains a materialized view which tracks the state of the Blog aggregate", "Java, Spring, Axonframework");
        webComponent.uses(blogPostQuerySideComponent, "Read materialized view");
        blogPostQuerySideComponent.uses(database, "Subscribes to event store and write materialized view");
        blogPostQuerySideComponent.setUrl("https://github.com/ivans-innovation-lab/my-company-blog-materialized-view");

        // ## Create views ##
        ViewSet views = workspace.getViews();
        // ### Static context view ###
        SystemContextView contextView = views.createSystemContextView(mySoftwareSystem, "Context", "The System Context diagram for the 'my-company' application");
        contextView.addAllElements();
        // ### Static container view ###
        ContainerView containerView = views.createContainerView(mySoftwareSystem, "Containers", "The Containers diagram for the 'my-company' application");
        containerView.addAllElements();
        // ### Static component view ###
        ComponentView componentView = views.createComponentView(webApplication,"Components", "The Components diagram for the 'my-company' application");
        componentView.addAllElements();
        


        
        // ## Styles ##
        Styles styles = views.getConfiguration().getStyles();
        styles.addElementStyle(Tags.ELEMENT).color("#000000");
        styles.addElementStyle(Tags.PERSON).background("#ffbf00").shape(Shape.Person);
        styles.addElementStyle(Tags.CONTAINER).background("#facc2E");
        styles.addElementStyle(MONOLITH_TAG).shape(Shape.Hexagon);
        styles.addElementStyle(DATASTORE_TAG).background("#f5da81").shape(Shape.Cylinder);
        styles.addRelationshipStyle(Tags.RELATIONSHIP).routing(Routing.Orthogonal);

        styles.addRelationshipStyle(Tags.ASYNCHRONOUS).dashed(true);
        styles.addRelationshipStyle(Tags.SYNCHRONOUS).dashed(false);

        uploadWorkspaceToStructurizr(workspace, WORKSPACE_ID, env.getProperty("spring.application.structurizr.apikey"), env.getProperty("spring.application.structurizr.apisecret"));
    }
    
    private static void uploadWorkspaceToStructurizr(Workspace workspace, Long workspaceId, String apiKey, String apiSecret) throws Exception {
        LOG.info("### Structurizr api Key: " + apiKey);
        LOG.info("### Structurizr api Secret: " + apiSecret);
        
        StructurizrClient structurizrClient = new StructurizrClient(apiKey, apiSecret);
        structurizrClient.setMergeFromRemote(true);
        structurizrClient.putWorkspace(workspaceId, workspace);
    }
    
    

}
