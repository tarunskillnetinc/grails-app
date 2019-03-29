package uk.co.wonderlane.wlpos

public class BackOfficeRabbitService extends RabbitService {

    public BackOfficeRabbitService(String host, String username, String password) {
        super(host, username, password, null, null);
    }

    @Override
    public void init() {
        super.init();
    }
}