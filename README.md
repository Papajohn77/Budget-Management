### Budget Management Service

---

The **Budget Management Service** empowers users to track their finances, manage expenses, and save money, either individually or within a group. Each user has full control over their personal account, recording income and expenses while organizing them into categories. For structured saving, users can create **Piggy Banks**, which function as dedicated savings accounts for specific financial goals. 

Upon account creation, each user automatically receives a **Savings** Piggy Bank, allowing them to store funds separately from their general available funds.

For collaborative financial planning, users can form or join **Groups**, where an **Admin** creates and manages the group and invites new members. Within a group, members can contribute to **Group Piggy Banks**, allowing collective savings toward shared financial goals. While all members can add funds, only the Admin can dissolve the piggy bank, ensuring structured financial oversight.

---
**Core Functionalities**:

**User registration, Accounts & Groups**: Users can register to the financial management system and form groups

**Financial Expense/Income entries**: Users can insert recurring or non-recurring financial records that belong to various categories

**Piggy Bank Creation**: Users can create one or multiple personal piggy banks that serve as dedicated saving accounts

**Savings**: Users can insert money in their savings. Savings piggy bank is automatically created upon registration.

---
#### **1. User registration, Accounts & Groups**
Users can create autonomous accounts in the financial management system by providing email and password.

Upon account creation, a **Savings** Piggy Bank is automatically assigned to each user, allowing them to store funds separately.

Each user can invite other users with autonomous accounts to join a Group. The user who sends the invitation becomes the **Admin** of the Group.

The user invited to join a group can Accept or Reject the invitation. The invitation states are: Pending, Accepted, Rejected. Every user can belong to many Groups and any user can create a Group and become its Admin.

The user account keeps a state of their Available Funds. This is calculated as following:
Income - (Expenses + Savings + Piggy Banks).

---

#### **2. Financial Expense/Income entries**
A user can add Expense or Income entries to the system, which can be either fixed or variable. Entries can also be recurring or non-recurring. Recurring entries have start and end date, and they are automatically displayed in the user’s records respectively. A User can decide to stop a recurring expense or income.

**Expenses** entries can belong to the following categories:
- Housing
- Entertainment
- Health
- Investments
- Food
- Transportation
- Other 

**Income** entries can belong to the following categories:
- Salary
- Dividends
- PassiveIncome
- Other

---

#### **3. Piggy Bank Creation**:
Users can create multiple individual **piggy banks** each having a dedicated goal (e.g. Travel, New Car etc.), as well as a target amount. Users can only add money to a piggy bank and not withdraw. They can however, dossolve the piggy bank whenever they want.

In case of Groups, only the Admin can create a **group piggy bank** for all Group members.
In a group piggy bank all members can only add money.
Only the Admin is eligible to dissolve the piggy bank (e.g. in case of an error or a change in goals). By this action, the amount of money will be returned to each member respectively.

A specific type of piggy bank are **Savings**, where users can both add or remove money if needed. In case of money removal, the amount returns in the available funding. Savings are individual, and cannot be created for a Group.

Users can only add money to Piggy Banks and Savings if they have sufficient available funds. This restriction does not apply when adding expenses.


---
## Domain Model

![image](DomainModelImage.png)

---
## Deliverables

The documents below extend the original monolith into a cloud-native, microservices-based system using Java, Quarkus, and MicroProfile, delivered in three stages: system design, implementation & testing, and cloud deployment with observability & fault tolerance.

- [Deliverable A — Microservices System Design](docs/deliverable-a.md)
- [Deliverable B — Microservices Implementation & Testing](docs/deliverable-b.md)
- [Deliverable C — Cloud Deployment on Kubernetes](docs/deliverable-c.md)
