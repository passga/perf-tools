package org.bonitasoft.audit.process;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.bonitasoft.deployer.DeployerHighLevelBuilder;
import org.bonitasoft.engine.api.APIClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditInjector {

    public static void main(String[] args) {
        /*  new Cli(args).parse();
         */
        final Logger log = LoggerFactory.getLogger(AuditInjector.class);


        final APIClient apiClient = new APIClient();
        try {
            File folder = new File("/tmp/bonita/audit/" + System.currentTimeMillis());
            folder.mkdirs();
            File profile = new File(AuditInjector.class.getClassLoader().getResource("profiles/profile-default.xml").getFile());
            File destProfile = new File(folder, "profile-audit.xml");
            Files.copy(profile.toPath(), destProfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            OrgaGenerator.getInstance().buildOrganization(2000).export(folder);
            ProcessGenerator.generate(folder).buildMassProcessDefinition(1);
          //  ProcessGenerator.generate(folder).buildSimpleProcessWithStringIndex();
            DeployerHighLevelBuilder.deployer("http://34.244.213.239:8080/bonita").httpTimeoutInSeconds(3400).username("install").password("install").build().deploy(folder);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
