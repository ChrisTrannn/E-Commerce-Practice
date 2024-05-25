- # General
    - #### Team#: 35 (team-sc)
    
    - #### Names: Chris Tran and Subin Kim
    
    - #### Project 5 Video Demo Link: 

    - #### Collaborations and Work Distribution:


- # Connection Pooling
    - #### Include the filename/path of all code/configuration files in GitHub of using JDBC Connection Pooling.
        - Configuration:
            - [WebContent/META-INF/context.xml](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/WebContent/META-INF/context.xml)
        - Servlets/Code:
            - [src/AddMovieServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/AddMovieServlet.java)
            - [src/AddStarServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/AddStarServlet.java)
            - [src/EmployeeLoginServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/EmployeeLoginServlet.java)
            - [src/GenreServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/GenreServlet.java)
            - [src/LoginServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/LoginServlet.java)
            - [src/MetadataServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/MetadataServlet.java)
            - [src/MovieSuggestion.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/MovieSuggestion.java)
            - [src/MoviesServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/MoviesServlet.java)
            - [src/PaymentServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/PaymentServlet.java)
            - [src/ShoppingCartServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/ShoppingCartServlet.java)
            - [src/SingleMovieServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/SingleMovieServlet.java)
            - [src/SingleStarServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/SingleStarServlet.java)
    
    - #### Explain how Connection Pooling is utilized in the Fabflix code.
        - Connection pooling is utilized in the Fabflix code by configuring the two DataSources to manage a pool
    of database connections. This allows for reuse of connections, which reduces overhead for every request
    and increases efficiency. There are two DataSources configure with connection pooling: one for the local database residing
    on the current instance, and one for the master database residing on the master instance (for write requests). The Connection Pooling
    configurations are in the context.xml file.
    
    - #### Explain how Connection Pooling works with two backend SQL.
        - Each backend database has their own DataSource configuration. In the context.xml file, there are two DataSources set up:
    one for database that resides on the current instance, and one for database that resides on the master instance. Each DataSources
    manages their own pool of connections. This greatly increases efficiency because both databases can handle many concurrent operations.
    There are two DataSources because all write requests are sent to the master database regardless, and all read requests
    can be directed to any database.
        

- # Master/Slave
    - #### Include the filename/path of all code/configuration files in GitHub of routing queries to Master/Slave SQL.
        - Configuration:
            - [WebContent/META-INF/context.xml](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/WebContent/META-INF/context.xml)
            - AWS Instance 1 Config:
                - /etc/apache2/sites-enabled/000-default.conf 
        - Servlets/Code:
            - [src/AddMovieServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/AddMovieServlet.java)
            - [src/AddStarServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/AddStarServlet.java)
            - [src/EmployeeLoginServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/EmployeeLoginServlet.java)
            - [src/GenreServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/GenreServlet.java)
            - [src/LoginServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/LoginServlet.java)
            - [src/MetadataServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/MetadataServlet.java)
            - [src/MovieSuggestion.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/MovieSuggestion.java)
            - [src/MoviesServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/MoviesServlet.java)
            - [src/PaymentServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/PaymentServlet.java)
            - [src/ShoppingCartServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/ShoppingCartServlet.java)
            - [src/SingleMovieServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/SingleMovieServlet.java)
            - [src/SingleStarServlet.java](https://github.com/UCI-Chenli-teaching/cs122b-s24-team-sc/blob/21c7716134a845df1e0e9949524b6e5f54096c12/src/SingleStarServlet.java)

    - #### How read/write requests were routed to Master/Slave SQL?
        - We routed write requests to the Master's SQL, but for read requests we routed them to either the Master or Slave SQL. We did this by adding an additional Data Source to the context.xml file and then in all the servlets that make write requests we only initialized and used the Master datasource to ensure that Master handled these writes. As for the servlets that only handle read requests, we initialized both the Master and Slave Data Sources and then randomly assigned these read servlets one of the two.
        - Additionally, we had to make sure that we created a new SQL user on the aws Instance 2 (Master) that is able to connect to the SQL server from Master's private IP Address
            - CREATE USER 'primary'@'masterPrivateIP' IDENTIFIED BY 'password';
            - GRANT ALL PRIVILEGES ON moviedb.* TO 'primary'@'masterPrivateIP';








-----------------Future Project 5----------------------------------    

- # JMeter TS/TJ Time Logs
    - #### Instructions of how to use the `log_processing.*` script to process the JMeter logs.


- # JMeter TS/TJ Time Measurement Report

| **Single-instance Version Test Plan**          | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTPS/10 threads                       | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 4: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |

| **Scaled Version Test Plan**                   | **Graph Results Screenshot** | **Average Query Time(ms)** | **Average Search Servlet Time(ms)** | **Average JDBC Time(ms)** | **Analysis** |
|------------------------------------------------|------------------------------|----------------------------|-------------------------------------|---------------------------|--------------|
| Case 1: HTTP/1 thread                          | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 2: HTTP/10 threads                        | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |
| Case 3: HTTP/10 threads/No connection pooling  | ![](path to image in img/)   | ??                         | ??                                  | ??                        | ??           |






-------old below (Project 3)------------------



# team-sc
[Project 3 Demo Video](https://www.youtube.com/watch?v=RJ2cYUDdCDg)
## Member Contributions:
Subin Kim: Prepared Statements, Adding HTTPS, Importing large XML data files into the Fabflix database \
Chris Tran: Adding reCAPTCHA, Use Encrypted Password, Implementing a Dashboard using Stored Procedure 

## Prepared Statements:
AddMovieServlet (CallableStatement for stored procedure)<br>
AddStarServlet (CallableStatement for stored procedure)<br>
EmployeeLoginServlet<br>
LoginServlet<br>
MoviesServlet<br>
PaymentServlet<br>
ShoppingCartServlet<br>
SingleMovieServlet<br>
SingleStarServlet<br>

## Two Parsing Time Optimization Strategies
1. In-Memory Hash Maps were used during our insertions into the database<br>
so that we could have constant-time lookup to check for existing records. Using this<br>
data structure saved us a lot of time as opposed to performing linear search. For example<br>
one use case of this was our _starNameToIdMap_ hash map that was created by looping through and mapping Star<br>
names to their id. This saved us time when we inserted into the _stars_in_movies_ table by quickly being able<br>
to perform a lookup to see if a star already existed from the stars table.
2. We implemented batches for our second technique so that we could treat<br>
lots of insertions into the database as a single transaction. Rather than individual<br>
transactions for thousands of data, we were able to decrease inserstion time by reducing<br>
the number of calls between the web application and database server.

## Inconsistent Data Reports from Parsing
-------------------------------------------------------------------
Report:<br>
1. Inserted **11882** movies<br>
2. Inserted **6839** stars<br>
3. Inserted **29468** stars_in_movies<br>
4. Inserted **11** genres<br>
5. Inserted **9670** genres_in_movies<br>
6. Total inconsistencies: **2068**<br>
7. Total duplicate movies: **278**<br>
8. Total duplicate stars: **24**<br>
9. Total stars not found: **16649**<br>
10. Total movies not found: **711**<br>
-------------------------------------------------------------------
## Design Decision
1. One of the inconsistencies that that we noticed is that the Casts124.xml file has some actors<br>
that do not exist in the Actors63.xml file. This allowed us to prevent insertion errors when we were<br>
adding entries into the Stars table.
2. There were a number of duplicate movies and stars that were prevalent in both the Casts124.xml and Actors63.xml<br>
which we made sure to skip to avoid duplicate primary key errors.
3. For other inconsistencies we skipped tags that had no IDs, movies with no titles, and movies with invalid years.
4. Another aspect we had to address were the <cat> or genre tags in the mains243.xml file. A lot of these tags were abbreviations<br>
in our existing column genres. We decided to create a mapping class that filtered and mapped extremely similar genres or abbreviations.
5. Employees could access Customer features, but Customers cannot access Employees

### Extra:
[AWS Deployment](https://54.176.86.219:8443/cs122b-s24-team-sc/login-page.html)

## Main branch for project 3

started dev branch
