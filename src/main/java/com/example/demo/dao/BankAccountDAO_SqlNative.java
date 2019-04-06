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
public class BankAccountDAO_SqlNative {

	/**
	 * dùng Spring IoC để khởi tạo biến này.
	 * javax.persistence.EntityManager;   là của JPA là interface, ko phải là Hibernate
	 *  đây là JPA interface để quản lý Socket connect, transaction gửi lệnh SQL và nhận trả về
	 *  Khi dùng JPA ta ko quan tâm tới Hibernate nữa giống như quan điểm của Common Logging vậy.
	 *  Tất cả Annotation và SQL sytax đều tuân thủ theo JPA.
	 *  Ta có thể thay Hibernate bằng 1 lib khác để implement JPA.
	 */
	@Autowired
	private EntityManager entityManager;   


	public BankAccountDAO_SqlNative() {
	}

	public List<BankAccountInfo> listBankAccountInfoByNativeSQL1(){

		//vd3:
		// BankAccountInfo.class phải chưa JPA annotation 
		Query q = entityManager.createNativeQuery("SELECT id, full_name, balance FROM bank_account WHERE id > :id", BankAccountInfo.class);
		q.setParameter("id", 1);  // ":id"
		List<BankAccountInfo> listAccounts = q.getResultList();  

		return listAccounts;
	}
	
	public List<BankAccountInfo> listBankAccountInfoByNativeSQL2(){

		// dùng Native SQL với JPA
		//    	Query q = entityManager.createNativeQuery("SELECT id, full_name, balance FROM bank_account");
		// a.id la ten column table ở SQL server (ko phải ở java class)
		Query q = entityManager.createNativeQuery("SELECT a.id, a.full_name, a.balance FROM bank_account a");

		//vd1: cách 1
		/*    	Query q = entityManager.createNativeQuery("SELECT id, full_name, balance FROM bank_account WHERE id > ?");
    	q.setParameter(1, 333);  // 1 = "?"  ở vị trí số 1
		 */

		//vd2: cách 2
		/*    	Query q = entityManager.createNativeQuery("SELECT id, full_name, balance FROM bank_account WHERE id > :id");
    	q.setParameter("id", 444);  // ":id"
		 */    	

		//trả về 1 list các ROW, mỗi row là 1 array Object[]
		List<Object[]> listObject = q.getResultList();  

		List<BankAccountInfo>  listAccounts = new ArrayList<BankAccountInfo>(); 

		for (Object[] a : listObject) {

			BankAccountInfo account = new BankAccountInfo();

			//phải ép kiểu
			account.setId(((BigInteger)a[0]).longValue());
			account.setFullName((String)a[1]);
			account.setBalance((Double)a[2]);
			listAccounts.add(account);

			System.out.println("id: " + a[0] +  ", full_name: " + a[1] + ", balance: "+ a[2]);
		}

		return listAccounts;
	}



}
