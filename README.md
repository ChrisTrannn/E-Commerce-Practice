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