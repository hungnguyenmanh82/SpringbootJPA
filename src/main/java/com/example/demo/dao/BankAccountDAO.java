package com.example.demo.dao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.BankAccount;
import com.example.demo.exception.BankTransactionException;
import com.example.demo.model.BankAccountInfo;

/**
 * Cần phải học JPA trước khi học Spring JPA
 * JPA là specification chuẩn, độc lập với Spring
 */
//@Repository là singleton
@Repository
public class BankAccountDAO {

	/**
	 * dùng Spring IoC để khởi tạo biến này.
	 * javax.persistence.EntityManager;   là của JPA là interface, ko phải là Hibernate
	 *  đây là JPA interface để quản lý Socket connect, transaction gửi lệnh SQL và nhận trả về
	 *  Khi dùng JPA ta ko quan tâm tới Hibernate nữa giống như quan điểm của Common Logging vậy.
	 *  Tất cả Annotation và SQL sytax đều tuân thủ theo JPA.
	 *  Ta có thể thay Hibernate bằng 1 lib khác để implement JPA.
	 */
//	@Autowired
	private EntityManager entityManager;   


	public BankAccountDAO() {
	}

	public BankAccount findById(Long id) { 
		return this.entityManager.find(BankAccount.class, id);
	}

	public List<BankAccountInfo> listBankAccountInfo() {

		/**
		 * dùng Syntax của JPA (JPQL) giống hệt với cú pháp của Hibernate là HQL
		 * ta chỉ quan tâm tới JPA và syntax của nó mà ko quan tâm tới Hibernate.
		 * các tên lấy theo JavaClass ko phải theo SQL column name
		 * 
		 * "Select new "  dùng khi muốn lấy 1 phần của Table và mapping với 1 class tương ứng BankAccountInfo.class
		 * "Select new " giống hệt với HQL của Hibernate
		 */
		String sql = "Select new " + BankAccountInfo.class.getName() //
				+ "(e.id,e.fullName,e.balance) " //
				+ " from " + BankAccount.class.getName() + " e ";

		//là của JPA là interface, ko phải là Hibernate
		Query query = entityManager.createQuery(sql, BankAccountInfo.class);

		return query.getResultList();
	}

	public List<BankAccountInfo> listBankAccountInfoByNativeSQL(){

		// dùng Native SQL với JPA
		//    	Query q = entityManager.createNativeQuery("SELECT id, full_name, balance FROM bank_account");
		Query q = entityManager.createNativeQuery("SELECT a.id, a.full_name, a.balance FROM bank_account a");

		//vd1:
		/*    	Query q = entityManager.createNativeQuery("SELECT id, full_name, balance FROM bank_account WHERE id > ?");
    	q.setParameter(1, 1);  // 1 = "?"
		 */

		//vd2:
		/*    	Query q = entityManager.createNativeQuery("SELECT id, full_name, balance FROM bank_account WHERE id > :id");
    	q.setParameter("id", 1);  // ":id"
		 */    	

		//vd3:
/*		Query q = entityManager.createNativeQuery("SELECT id, full_name, balance FROM bank_account WHERE id > :id", BankAccountInfo.class);
		q.setParameter("id", 1);  // ":id"
		List<BankAccountInfo> listObject = q.getResultList();  */

		//trả về 1 list các ROW, mỗi row là 1 array 
		List<Object[]> listObject = q.getResultList();  

		List<BankAccountInfo>  listAccounts = new ArrayList<BankAccountInfo>(); 

		for (Object[] a : listObject) {

			BankAccountInfo account = new BankAccountInfo();

			account.setId(((BigInteger)a[0]).longValue());
			account.setFullName((String)a[1]);
			account.setBalance((Double)a[2]);
			listAccounts.add(account);

			System.out.println("id: " + a[0] +  ", full_name: " + a[1] + ", balance: "+ a[2]);
		}

		return listAccounts;
	}

	// MANDATORY: Transaction must be created before.
	// nếu chưa có transaction nào tồn thì throw exception (ko tạo mới transaction nếu ko tồn tại)
	@Transactional(propagation = Propagation.MANDATORY )  
	public void addAmount(Long id, double amount) throws BankTransactionException {
		BankAccount account = this.findById(id);
		if (account == null) {
			throw new BankTransactionException("Account not found " + id);
		}
		double newBalance = account.getBalance() + amount;
		if (account.getBalance() + amount < 0) {
			throw new BankTransactionException(
					"The money in the account '" + id + "' is not enough (" + account.getBalance() + ")");
		}
		account.setBalance(newBalance);
	}

	// Do not catch BankTransactionException in this method.
	@Transactional(propagation = Propagation.REQUIRES_NEW,  //tạo mới transaction
			rollbackFor = BankTransactionException.class)
	public void sendMoney(Long fromAccountId, Long toAccountId, double amount) throws BankTransactionException {

		addAmount(toAccountId, amount);
		addAmount(fromAccountId, -amount);
	}

}
