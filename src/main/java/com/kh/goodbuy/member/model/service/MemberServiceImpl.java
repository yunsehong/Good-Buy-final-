package com.kh.goodbuy.member.model.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.kh.goodbuy.member.model.dao.MemberDao;
import com.kh.goodbuy.member.model.vo.Member;
import com.kh.goodbuy.member.model.vo.MyTown;

@Service
public class MemberServiceImpl implements MemberService {
	@Autowired
	private MemberDao mDao;
	
	@Override
	public Member loginMember(Member m) {
		// TODO Auto-generated method stub
		return mDao.selectMember(m);
	}
	@Override
	public int insertMember(Member m) {
		// TODO Auto-generated method stub
		
		return mDao.insertMember(m);
	}
	@Override
	public int insertMyTown(MyTown mt) {
		return  mDao.insertMember(mt);
	}
	
	@Override
	public int insertMember(ArrayList<Object> list) {
		return mDao.insertMember(list);
	}
	@Override
	public List<Member> selectMemberList() {
		// TODO Auto-generated method stub
		return mDao.selectList();
	}
	

}
