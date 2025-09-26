module lk.ijse.elite {
    requires java.sql;
    requires java.naming;

    requires org.hibernate.orm.core;
    requires jakarta.persistence;

    requires javafx.controls;
    requires javafx.fxml;

    requires org.slf4j;

    requires jakarta.mail;
    requires jakarta.activation;
    requires static lombok;
    requires bcrypt;

    exports lk.ijse.elite to javafx.graphics;
    exports lk.ijse.elite.dto;


    opens lk.ijse.elite.controller to javafx.fxml;
    opens lk.ijse.elite.entity to org.hibernate.orm.core;
}
