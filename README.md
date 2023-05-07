# Scala REST API Application with Play Framework

### Technology Stack
- Scala 2.13
- Play Framework 2.8.19
- Slick 5.0.0 (DB Access/Evolutions)
- PostgreSQL
- Guice (DI)
- Silhouette (Authn/Authz)
- HTTPClient (Play WS)
- JSON Conversion (Play JSON)
- Logging (Logback)

### Project Structure
```
── scala-demo
   ├── app                                            # The Scala application source code
   │   ├── utils
   │   │   └── auth                                   # Authentication utils
   │   ├── domain
   │   │   ├── tables                                 # Slick tables
   │   │   │   ├──OrderDetailTable.scala                 # Represents order details table
   │   │   │   ├── OrderDetailTable.scala             # Represents order details table
   │   │   │   ├── ProductTable.scala                 # Represents products table
   │   │   │   └── UserTable.scala                    # Represents users table
   │   │   ├── models                                 # Models
   │   │   │   ├── Order.scala                        # Order model
   │   │   │   ├── OrderDetail.scala                  # OrderDetail model
   │   │   │   ├── Product.scala                      # Product model   
   │   │   │   └── User.scala                         # User model
   │   │   ├── daos                                   # Data access objects
   │   │   │   ├── DaoRunner.scala                    # Run Slick database actions by transactions
   │   │   │   ├── DbExecutionContext.scala           # Custom ExecutionContext for running DB connections
   │   │   │   ├── PasswordInfoDao.scala              # Password dao
   │   │   │   ├── PostDao.scala                      # Post dao
   │   │   │   └── UserDao.scala                      # User dao
   │   │   └── dtos                                   # Data transfer objects
   │   │       ├── request                            # Request dtos
   │   │       │   ├── LoginPostRequest.scala         # Request dto for login
   │   │       │   ├── OrderItemsRequest.scala        # Request dto for create/update order items
   │   │       │   ├── OrderPostRequest.scala         # Request dto for create order
   │   │       │   ├── OrderPutRequest.scala          # Request dto for update order
   │   │       │   ├── ProductPostRequest.scala       # Request dto for create/update product
   │   │       │   └── UserPostRequest.scala          # Request dto for create/update user
   │   │       └── response                           # Response dtos
   │   │           ├── OrderItemsResponse.scala       # Response dto for order items
   │   │           ├── OrderResponse.scala            # Response dto for order
   │   │           ├── ProductResponse.scala          # Response dto for product
   │   │           └── UserResponse.scala             # Response dto for user
   ├── httpclient                                     # Play modules
   │   │   ├── AbstractHttpClient.scala
   │   │   ├── ErrorResponse.scala
   │   │   ├── ExternalProductClient.scala
   │   │   └── ExternalServiceException.scala
   │   ├── system                                     # Play modules
   │   │   └── modules
   │   │       └── SilhouetteModule.scala             # Bind silhouette components
   │   └── controllers                                # Application controllers
   │       ├── AuthenticationController.scala         # Login/Register controllers
   │       ├── SilhouetteController.scala             # Abstract silhouette controller
   │       ├── ProductController.scala                # Product controllers for CRUD a product
   │       ├── OrderController.scala                  # Order controllers for CRUD an order
   │       └── UserController.scala                   # User controllers for CRUD an user
   ├── test
   ├── conf
   │   ├── evolutions                                 # Play evolutions SQL queries
   │   │   └── default                                # Default database
   │   │       └── 1.sql                              # Creates db tables
   │   ├── application.conf                           # Play configuration
   │   ├── default.conf                               # All aplication default configurations
   │   ├── routes                                     # Play routing
   │   ├── database.conf                              # Database configuration
   │   ├── httpclient.conf                            # Config for external API
   │   └── silhouette.conf                            # Silhouette configuration
   ├── postgresql
   │   └── postgresql.conf                            # PostgreSQL configuration
   ├── postman
   │   └── Scala.postman_collection.josn              # Postman collection
   ├── project
   ├── .gitignore
   ├── build.sbt
   ├── build.sc
   └── docker-compose.yml                             # Docker compose for PostgreSQL
```

### Getting Started

#### 1. Setup `PostgreSQL` Database
You can install PostgreSQL on your local machine (need to update connection strings in `./conf/application.conf`) or running the docker compose in the `root` folder
to get PostgreSQL ready.

#### 2. Run application
You need to download and install JDK 11 and sbt for this application to run.
Once you have sbt installed, the following at the command prompt will start up Play in development mode:
```bash
./sbt run
```

Play will start up on the HTTP port at <http://localhost:9000/>.   You don't need to deploy or reload anything -- changing any source code while the server is running will automatically recompile and hot-reload the application on the next HTTP request.

#### 3. Run Unit Tests
```bash
./sbt clean test
```

or To generate code coverage report with SCoverage
```bash
./sbt clean coverage test coverageReport
```

#### 4. Run Integration Tests
```bash
./sbt clean integration/test
```

### Usage
1. Import Postman collection at `postman` folder and create a new environment with the ```X-Auth``` variable
2. Using `POST /login` endpoint to login with Admin account ```admin@nashtechglobal.com | Admin@123456``` to get JWT token in `X-Auth` response header
3. Do any things you want with the application

### References
- [Scala Demo](https://github.com/nashtech-garage/scala-demo)

### License & Copyright
&copy; 2023 Khoa Dang Hoang khoahd7621
> :love_you_gesture: Feel free to use my repository and star it if you find something interesting :love_you_gesture: