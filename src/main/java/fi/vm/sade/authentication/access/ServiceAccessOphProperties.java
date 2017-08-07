package fi.vm.sade.authentication.access;

import fi.vm.sade.properties.OphProperties;
import java.nio.file.Paths;

public class ServiceAccessOphProperties extends OphProperties {

    public ServiceAccessOphProperties() {
        addFiles("/service-access-oph.properties");
        addOptionalFiles(Paths.get(System.getProperties().getProperty("user.home"), "/oph-configuration/common.properties").toString());
    }

}
