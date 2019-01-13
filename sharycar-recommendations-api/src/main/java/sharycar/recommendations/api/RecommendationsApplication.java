package sharycar.catalogue.api;


/**
 * Author Jaka Krajnc
 */

import com.kumuluz.ee.discovery.annotations.RegisterService;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@RegisterService
@ApplicationPath("/")
public class CatalogueApplication extends Application{
}
