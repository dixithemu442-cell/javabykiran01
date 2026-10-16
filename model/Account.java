package model;


public class Account {
	 private String accountNumber;
	private String accountHolderName ;
	private String aadhaarNumber;
   private  String mobileNumber;
    private double balance ;



 public Account(String accountNumber,String accountHolderName,String aadhaarNumber,String mobileNumber,double balance){
this.accountNumber=accountNumber;
this.accountHolderName=accountHolderName;
this.aadhaarNumber=aadhaarNumber;
this.mobileNumber=mobileNumber;
this.balance=balance;
}
	

public String getAccountNumber() {
	return accountNumber;
}

public String getAccountHolderName() {
	return accountHolderName;
}
	
	
	public String getMobileNumber() {
		return mobileNumber;
		
	}
	
	public String getAadhaarNumber() {
		return aadhaarNumber;
		
	}
	public double getBalance() {
		return balance;
		
	}
	
	 public void deposit(double amount)  {
		 if(amount <=0) {
			 System.out.println("Invalid amount");
			 return;
		 }
		 
		 balance=balance+amount ;
		 System.out.println("Deposit Successfully");
		 System.out.println(" Now Your Balance is :- "+balance);
	 }
	 public void withdrow(double amount) {
		  if (amount<=0) {
			  System.out.println("Invalid withdrawal amount ");
			  return;
		 
		  }
		  if (amount>balance) {
			  System.out.println("Insufficient balance");
			  return;
			  
		  }
		  balance=balance-amount;
		  System.out.println("withdrawal successful");
		  System.out.println("Your balance is "+balance);
}
	 
}


