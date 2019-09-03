package org.bonitasoft.audit.process;

import java.io.File;

import com.bonitasoft.deployer.DeployerHighLevelBuilder;
import org.bonitasoft.engine.api.APIClient;

public class AuditInjector {
    public static void main(String[] args) {
        final APIClient apiClient = new APIClient();
        try {
            File folder = new File("/tmp/bonita/audit/" + System.currentTimeMillis());
            folder.mkdirs();
            OrgaGenerator.createInstance().buildOrganization(200).export(folder);
            ProcessGenerator.createInstance().generate().toBarFiles(folder);
            DeployerHighLevelBuilder.deployer("http://localhost:23667/bonita").password("install").password("install").build().deploy(folder);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
