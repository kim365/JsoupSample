package com.image.image.search.widget;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;

import com.framework.base.BaseActivity;
import com.framework.utils.UIUtils;
import com.framework.widget.LoadMoreRecyclerView;
import com.image.R;
import com.image.image.detail.widget.ImageDetailActivity;
import com.image.image.search.model.SearchListModel;
import com.image.image.search.presenter.SearchListPresenter;
import com.image.image.search.presenter.SearchListPresenterImpl;
import com.image.image.search.view.SearchListView;
import com.socks.library.KLog;

import java.util.ArrayList;
import java.util.List;

/**
 * by y on 2017/4/19.
 */

public class SearchListActivity extends BaseActivity
        implements
        SwipeRefreshLayout.OnRefreshListener,
        SearchListView {

    private static final String SEARCH_TYPE = "search";
    private static final String SEARCH_CONTENT = "content";
    private String searchType;
    private String content;

    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LoadMoreRecyclerView recyclerView;
    private SearchListAdapter adapter;

    private SearchListPresenter presenter;
    private int page = 1;

    public static void start(String searchType, String content) {
        Bundle bundle = new Bundle();
        bundle.putString(SEARCH_TYPE, searchType);
        bundle.putString(SEARCH_CONTENT, content);
        UIUtils.startActivity(SearchListActivity.class, bundle);
    }

    @Override
    protected void initCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            searchType = extras.getString(SEARCH_TYPE);
            content = extras.getString(SEARCH_CONTENT);
        }
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setTitle(searchType + " (关键词:" + content + ")");
        presenter = new SearchListPresenterImpl(this);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(this::onRefresh);
        initRecyclerView();
    }

    private void initRecyclerView() {
        adapter = new SearchListAdapter(new ArrayList<>());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setLoadingMore(() -> {
            KLog.i(adapter.getTotalPage());
            if (page == adapter.getTotalPage()) {
                UIUtils.snackBar(getView(R.id.coordinatorLayout), getString(R.string.data_empty));
                return;
            }
            ++page;
            presenter.netWorkRequest(searchType, content, page);
        });
        adapter.setOnItemClickListener(
                (view, position, info) -> ImageDetailActivity.startIntent(searchType, info.detailUrl));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void initById() {
        toolbar = getView(R.id.toolbar);
        swipeRefreshLayout = getView(R.id.srf_layout);
        recyclerView = getView(R.id.recyclerView);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_search;
    }

    @Override
    public void onRefresh() {
        page = 1;
        presenter.netWorkRequest(searchType, content, page);
    }

    @Override
    public void netWorkSuccess(List<SearchListModel> data) {
        if (page == 1) {
            adapter.removeAll();
        }
        adapter.addAll(data);
    }

    @Override
    public void netWorkError() {
        UIUtils.snackBar(getView(R.id.coordinatorLayout), getString(R.string.network_error));
    }

    @Override
    public void showProgress() {
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void noMore() {
        UIUtils.snackBar(getView(R.id.coordinatorLayout), getString(R.string.data_empty));
    }
}