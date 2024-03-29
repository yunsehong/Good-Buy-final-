package com.kh.goodbuy.goods.model.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kh.goodbuy.business.model.vo.Review;
import com.kh.goodbuy.common.model.vo.Keyword;
import com.kh.goodbuy.common.model.vo.Reply;
import com.kh.goodbuy.common.model.vo.Report;
import com.kh.goodbuy.goods.model.dao.GoodsDao;
import com.kh.goodbuy.goods.model.vo.Addfile;
import com.kh.goodbuy.goods.model.vo.Gcate;
import com.kh.goodbuy.goods.model.vo.Goods;
import com.kh.goodbuy.member.model.vo.PageInfo;
import com.kh.goodbuy.member.model.vo.Search;
import com.kh.goodbuy.town.model.vo.Town;

@Service
public class GoodsServiceImpl implements GoodsService {
	@Autowired
	private GoodsDao gDao;

	@Override
	public List<Gcate> selectCate() {

		return gDao.selectCate();
	}

	@Override
	public int insertGoods(Goods g, List<Addfile> list) {
		int result = gDao.insertGoods(g);
		if (result > 0)
			return gDao.insertFile(list);
		else
			return 0;

	}

	@Override
	public int selectCateNo(Gcate c) {
		// TODO Auto-generated method stub
		return gDao.selectCateNo(c);
	}

	@Override
	public int insertFile(List<Addfile> list) {
		// TODO Auto-generated method stub
		return gDao.insertFile(list);
	}

	@Override
	public int selectAllCount() {
		// TODO Auto-generated method stub
		return gDao.selectAllCount();
	}

	@Override
	public int selectListCount(Town m) {
		// TODO Auto-generated method stub
		return gDao.selectListCount(m);
	}

	@Override
	public List<Goods> selectAllList(PageInfo pi) {
		// TODO Auto-generated method stub
		return gDao.selectAllList(pi);
	}

	@Override
	public List<Goods> selectList(Town m, PageInfo pi) {
		// TODO Auto-generated method stub
		return gDao.selectList(m, pi);
	}

	@Override
	public int selectMyListCount(String user_id) {
		// TODO Auto-generated method stub
		return gDao.selectMyListCount(user_id);
	}

	@Override
	public List<Goods> selectMyList(String user_id, PageInfo pi) {
		// TODO Auto-generated method stub
		return gDao.selectMyList(user_id, pi);
	}

	@Override
	public Town selectSecondTown(String user_id) {
		// TODO Auto-generated method stub
		return gDao.selectSecondTown(user_id);
	}

	@Override
	public int selectCateCount(String cate) {
		// TODO Auto-generated method stub
		return gDao.selectCateCount(cate);
	}

	@Override
	public List<Goods> selectCateList(PageInfo pi, String cate) {
		// TODO Auto-generated method stub
		return gDao.selectCateList(pi, cate);
	}

	@Override
	public int selectCateCount2(Town myTown, String cate) {
		// TODO Auto-generated method stub
		return gDao.selectCateCount2(myTown, cate);
	}

	@Override
	public List<Goods> selectCateList2(Town myTown, PageInfo pi, String cate) {
		// TODO Auto-generated method stub
		return gDao.selectCateList2(myTown, pi, cate);
	}

	@Override
	public int selectMyCateListCount(String user_id, String cate) {
		// TODO Auto-generated method stub
		return gDao.selectMyCateListCount(user_id, cate);
	}

	@Override
	public List<Goods> selectMyCateList(String user_id, PageInfo pi, String cate) {
		// TODO Auto-generated method stub
		return gDao.selectMyCateList(user_id, pi, cate);
	}

	@Override
	public Goods Goodsdetail(int gno) {
		// TODO Auto-generated method stub
		return gDao.Goodsdetail(gno);
	}

	@Override
	public Goods GoodsMydetail(int gno) {
		// TODO Auto-generated method stub
		return gDao.GoodsMydetail(gno);
	}

	@Override
	public int likeGoods(int gno, String user_id) {
		// TODO Auto-generated method stub
		return gDao.likeGoods(gno, user_id);
	}

	@Override
	public int deleteLike(int gno, String user_id) {
		// TODO Auto-generated method stub
		return gDao.deleteLike(gno, user_id);
	}

	@Override
	public int insertLike(int gno, String user_id) {
		// TODO Auto-generated method stub
		return gDao.insertLike(gno, user_id);
	}

	@Override
	public List<Reply> insertReply(Reply r, Goods g) {
		System.out.println("댓글작성자 "+r);
		int result = gDao.insertReply(r, g);
		if(result >0) {
			gDao.insertPoint(r.getUser_id());
		}
		return gDao.selectReplyList(g);
	}

	@Override
	public List<Reply> selectReplyList(Goods g) {
		// TODO Auto-generated method stub
		return gDao.selectReplyList(g);
	}

	@Override
	public List<Goods> selectMySellingList(String user_id, PageInfo pi) {
		return gDao.selectMySellingList(user_id, pi);

	}

	@Override
	public int changeGoodsStatus(Goods g, String status) {
		return gDao.changeGoodsStatus(g, status);
	}

	@Override
	public int selectMyHiddenListCount(String user_id) {
		return gDao.selectMyHiddenListCount(user_id);
	}

	@Override
	public List<Goods> selectMyHiddenList(String user_id, PageInfo pi) {
		return gDao.selectMyHiddenList(user_id, pi);
	}

	@Override

	public int selectMyLikeGoodsCount(String user_id) {
		return gDao.selectMyLikeGoodsCount(user_id);
	}

	@Override
	public List<Goods> selectMyLikeGoodsList(String user_id, PageInfo pi) {
		return gDao.selectMyLikeGoodsList(user_id, pi);
	}

	public int deleteGoods(int gno) {
		// TODO Auto-generated method stub
		return gDao.deleteGoods(gno);
	}

	@Override
	public int hideGoods(int gno) {
		// TODO Auto-generated method stub
		return gDao.hideGoods(gno);
	}

	@Override
	public int payUpPoint(Integer gno, String user_id) {
		int result = gDao.upGoods(user_id);
		return gDao.payUpPoint(gno);

	}

	@Override
	public List<Goods> selectGoodsList(PageInfo pi) {
		// TODO Auto-generated method stub
		return gDao.selectGoodsList(pi);
	}

	@Override
	public List<String> selectLikeGoods(int gno) {
		// TODO Auto-generated method stub
		return gDao.selectLikeGoods(gno);
	}

	@Override
	public int updateGoods(Goods g, List<Addfile> list) {
		if(list!=null) {
			gDao.updateFile(list, g.getGno());
		}
		return gDao.updateGoods(g);
	}

	@Override
	public int selectMyDealListCount(String user_id) {
		return gDao.selectMyDealListCount(user_id);
	}

	@Override
	public List<Goods> selectMyDealList(String user_id, PageInfo pi) {
		return gDao.selectMyDealList(user_id, pi);

	}

	@Override
	public Goods selectProduct(int gno) {
		// TODO Auto-generated method stub
		return gDao.selectProduct(gno);
	}

	@Override
	public List<Goods> searchList(Search search) {
		// TODO Auto-generated method stub
		return gDao.searchList(search);
	}

	@Override
	public List<Reply> updateReply(int rno, Goods g) {
		gDao.updateReply(rno);
		return gDao.selectReplyList(g);
	}


	@Override
	public int updateProduct(int gno) {
		// TODO Auto-generated method stub
		return gDao.updateProduct(gno);
	}

	@Override
	public int updateProduct2(int gno) {
		// TODO Auto-generated method stub
		return gDao.updateProduct2(gno);
	}

	@Override
	public int deleteFile(String index) {
		// TODO Auto-generated method stub
		return gDao.deleteFile(index);
	}

	@Override
	public int selectSearchCount(String search) {
		// TODO Auto-generated method stub
		return gDao.selectSearchCount(search);
	}

	@Override
	public List<Goods> selectSearchList(PageInfo pi, String search) {
		// TODO Auto-generated method stub
		return gDao.selectSearchList(pi, search);
	}

	@Override
	public int selectMySearchCount(String search, Town myTown) {
		// TODO Auto-generated method stub
		return gDao.selectMySearchCount(search, myTown);
	}

	@Override
	public List<Goods> selectMySearchList(PageInfo pi, String search, Town myTown) {
		// TODO Auto-generated method stub
		return gDao.selectMySearchList(pi, search, myTown);
	}

	@Override
	public List<Goods> selectSellingList(String user_id) {
		// TODO Auto-generated method stub
		return gDao.selectSellingList(user_id);
	}

	@Override
	public List<Review> selectReviewList(String seller) {
		// TODO Auto-generated method stub
		System.out.println("seller는 : "+seller);
		return gDao.selectReviewList(seller);
	}

	@Override
	public int insertKeywordAlarm(Goods g) {
		return gDao.insertKeywordAlarm(g);
	}
	@Override
    public List<Review> insertReview(Review r, String userId) {
		//댓글 인서트 먼저
		int result = gDao.insertReview(r);
		if(result>0) {
			System.out.println("seller는 : "+userId);
			return gDao.selectReviewList(userId);
		}
		else {
			
			return null;
		}
	}

	@Override
	public List<Review> deleteReview(int rno, String seller) {
		// 댓글 삭제 
		int result = gDao.deleteReview(rno);
		
		if(result>0) {
			System.out.println("seller는 : "+seller);
			return gDao.selectReviewList(seller);
		}
		else {
			
			return null;
		}
	}

	@Override
	public int productreportupdate(int gno) {
		// TODO Auto-generated method stub
	 return gDao.productreportupdate(gno);
	}

	@Override
	public int reviewOk(String seller, String user_id) {
		// TODO Auto-generated method stub
		return gDao.reviewOk(seller, user_id);
	}

	@Override
	public List<Goods> selectGoodsRankList() {
		return gDao.selectGoodsRankList();
	}

	@Override
	public List<Keyword> selectBestKeyword() {
		return gDao.selectBestKeyword();
  }
  
 @Override
  public List<Goods> selectGoodsSrcList(int re_no) {
		// TODO Auto-generated method stub
		return gDao.selectGoodsSrcList(re_no);
	}

	@Override
	public Goods Goodsreportdetail(int re_no) {
		int gno =  gDao.Goodsreportdetail(re_no);
		System.out.println("gno : " + gno);
		return gDao.Goodsdetail(gno);
	}

	

}
