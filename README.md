### Budget Management Service

---

The **Budget Management Service** empowers users to track their finances, manage expenses, and save money, either individually or within a group. Each user has full control over their personal account, recording income and expenses while organizing them into categories. For structured saving, users can create **Piggy Banks**, which function as dedicated savings accounts.

For collaborative financial planning, users can form or join **Groups**, where an **Admin** manages the group’s settings and invites new members. Within a group, members can contribute to **Group Piggy Banks**, allowing collective savings toward shared financial goals. While all members can add funds, only the Admin can manage withdrawals or dissolve the piggy bank, ensuring structured financial oversight.

To enhance financial awareness, the platform generates **Reports**, offering both individual and group-level insights. Users can analyze monthly and annual spending trends, track savings progress, and optimize their budgeting strategies. This structured yet flexible system supports both personal financial independence and collaborative money management.

---

#### **Users & Accounts**
- Users create autonomous accounts.  
- Each user can:
  - Invite other users with autonomous accounts to join a **Group**. The user who sends the invitation becomes the **Admin** of the Group.
  - Accept or reject invitations to join a Group (invitation states: `Pending`, `Accepted`, `Rejected`).  
- A user can belong to **only one Group**.

---

#### **Transactions**
- Users record **expenses** and **income**:
  - Income/expenses can be either **variable** or **fixed**.
  - They belong to the following categories:  
    - Housing
	- Entertainment
	- Health & Medical
	- Investments
	- Food
	- Transportation
	- Salary
	- Dividents
	- Passive Income
    - Others  

- **Piggy Banks**:
  - Users can create **individual piggy banks**.  
  - Only the **Admin** can create **group piggy banks** for all Group members.  
  - In a group piggy bank:
    - All members of the Group can **only add money**.  
    - Only the Admin can "break" the piggy bank, returning the money to the members (e.g., in case of an error or a change in goals).  
  - **Individual savings**: A type of piggy bank where users can both add and withdraw money as needed.  

- **Available Funds**:  
  Income - (Expenses + Savings + Piggy Banks).  

---

#### **Reports**
- **Individual Reports**:
  - **Monthly**:
    - Monthly budget (Income - Expenses).  
    - Expenses by category.  
    - Savings goals.  
  - **Annual**:
    - Expenses by category.  
    - Savings goals.  

- **Group Reports**:
  - **Monthly**:
    - Expenses by category.  
    - Savings goals.  
  - **Annual**:
    - Expenses by category.  
    - Savings goals.  

---
## Domain Model

![image](DomainModelImage.png)