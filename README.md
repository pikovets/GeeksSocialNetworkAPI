## 1. Introduction
The backend of **GeeksSocialNetwork** is designed to provide a robust and scalable architecture that supports the platform's core functionalities. Built using modern Java technologies and frameworks, the backend handles essential operations related to user management, community interactions, and content creation, ensuring a seamless experience for users.

At the heart of this architecture is the **Spring Boot** framework, which facilitates the development of RESTful APIs for various services. These services include user authentication, post management, commenting, and community features, all structured to enable efficient data handling and interaction. The backend also implements comprehensive security measures through JWT (JSON Web Token) authentication, ensuring that user data and interactions remain secure and protected.

For ease of development and integration, the backend provides an interactive API documentation interface through Swagger. This documentation allows developers to explore available endpoints, view request/response formats, and test API calls directly from the browser. The Swagger UI can be accessed at the following URL: http://localhost:3000/swagger-ui/index.html.

### 1.1 Technologies Used

**Java:** The primary programming language for developing the backend services.

**Spring Boot:** A framework for building RESTful APIs with ease, providing features like dependency injection and configuration management.

**Spring Security:** A powerful and customizable authentication and access control framework for securing the application.

**JPA (Java Persistence API):** Used for managing relational data in Java applications. This project utilizes Hibernate as the implementation for ORM (Object-Relational Mapping).

**PostgreSQL:** An advanced open-source relational database management system used for data persistence.


## 2. Cloning the Repositories
To run this project you need to clone all parts of this project into one folder:
```
git clone https://github.com/pikovets/GeeksSocialNetworkAPI.git
git clone https://github.com/pikovets/GeeksSocialNetworkUI.git
git clone https://github.com/pikovets/GeeksSocialNetworkTests.git
```

## 3. Project Structure Overview

**Backend:** Describe the backend project, its technology stack, and the main functionality<br>

**Frontend:** Describe the frontend project, its technology stack, and main features<br>

**Selenium Tests:** Explain the Selenium tests project, what it covers, and how it interacts with the frontend and backend<br>

**docker-compose.yml:** File to start all services with one command<br>

## 4. Explaining  Docker Compose File
The docker-compose.yml file sets up the necessary services for the project. Here’s a breakdown of each service:

```
version: '3.9'
services:
  frontend:
    container_name: 'frontend'
    build: ./frontend
    depends_on:
      - backend
    ports:
      - "8080:8080"
    networks:
      - my_network
      
  backend:
    container_name: 'backend'
    build: ./backend
    environment:
      DATABASE_HOST: postgres
    depends_on:
      - postgres
    ports:
      - "3000:3000"
    networks:
      - my_network
      
  postgres:
    container_name: 'postgres'
    image: postgres
    restart: always
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
      POSTGRES_DB: social_network_db
    volumes:
      - ./postgres/docker_postgres_init.sql:/docker-entrypoint-initdb.d/docker_postgres_init.sql
      - ./postgres/data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - my_network
      
  rabbitmq:
    hostname: "rabbitmq"
    container_name: 'rabbit-mq'
    image: rabbitmq:3-management-alpine
    restart: always
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - my_network
      
  chrome:
    image: selenium/node-chrome:dev
    container_name: 'chrome-browser'
    shm_size: 2gb
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4442
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4443
    networks:
      - my_network

  selenium-hub:
    image: selenium/hub:latest
    container_name: 'selenium-hub'
    ports:
      - "4442:4442"
      - "4443:4443"
      - "4444:4444"
    networks:
      - my_network

volumes:
  rabbitmq_data:

networks:
  my_network:
    driver: bridge
```

Service Descriptions:

**Frontend:** Serves the user interface and depends on the backend service.

**Backend:** Manages data operations and provides API access, dependent on PostgreSQL.

**Postgres:** The database service used for data persistence.

**RabbitMQ:** Message broker for handling asynchronous communications.

**Chrome and Selenium Hub:** Provide the environment for running Selenium tests.

## 5. Running the Projects
Command to build and run the Docker Compose setup:
```docker-compose up --build```

## 6. Stopping and Cleaning Up
Command to stop the containers:<br>
```docker-compose down```

Command to remove all containers, networks, and volumes to start fresh: <br>
```docker-compose down --volumes --remove-orphans```
