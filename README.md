# Account Service application

This is REST application where you can create users, assign them roles, add payments and so on.

### Roles & endpoints:

There are 4 roles: Admin, User, Accountant, Auditor. The first created user will have ADMIN role, all others - USER. It's forbidden to combine ADMIN role with any other. And for better testing, ADMIN has ALL rights (you can change it manually if needed).

01. POST api/auth/signup | Any // registration
02. POST api/auth/changepass | User, Accountant, Admin // change password
03. GET /api/admin/user | Admin // Get all users
04. POST api/acct/payments | Accountant, Admin // add list of payments
05. GET api/acct/payments | Accountant, Admin  // get all payments
06. GET api/empl/payment | User, Accountant, Admin // get payments for employee
07. PUT api/acct/payments | Accountant, Admin // change payment for employee
08. PUT api/admin/user/role | Admin // change user role
09. DELETE api/admin/user | Admin // delete user
10. PUT /api/admin/user/access | Admin // lock or unlock user
11. GET /api/security/events | Any authenticated // get all events

**PS.** you can find all Postman requests with bodies/params in resource folder. 

#### Stack:
- Java 17
- Spring security
- Spring JPA
- H2 Database

#### Tom Shandrikov, 28.11.2022