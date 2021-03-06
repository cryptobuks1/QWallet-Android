package com.stratagile.qlink.ui.activity.rank;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.socks.library.KLog;
import com.stratagile.qlink.R;
import com.stratagile.qlink.application.AppConfig;
import com.stratagile.qlink.base.BaseFragment;
import com.stratagile.qlink.entity.ActiveList;
import com.stratagile.qlink.ui.activity.rank.component.DaggerRankListComponent;
import com.stratagile.qlink.ui.activity.rank.contract.RankListContract;
import com.stratagile.qlink.ui.activity.rank.module.RankListModule;
import com.stratagile.qlink.ui.activity.rank.presenter.RankListPresenter;
import com.stratagile.qlink.ui.adapter.active.NewRankListAdapter;
import com.stratagile.qlink.ui.adapter.active.RankListAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author hzp
 * @Package com.stratagile.qlink.ui.activity.rank
 * @Description: $description
 * @date 2018/07/31 18:09:12
 */

public class RankListFragment extends BaseFragment implements RankListContract.View {

    @Inject
    RankListPresenter mPresenter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    private RankViewModel viewModel;
    private RankListAdapter rankListAdapter;
    private NewRankListAdapter newRankListAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rank_list, null);
        ButterKnife.bind(this, view);
        Bundle mBundle = getArguments();
        viewModel = ViewModelProviders.of(getActivity()).get(RankViewModel.class);
        rankListAdapter = new RankListAdapter(new ArrayList<>());
        newRankListAdapter = new NewRankListAdapter(new ArrayList<>());
        recyclerView.setAdapter(rankListAdapter);
        setListener();
        return view;
    }

    private void setListener() {
        viewModel.getCurrentAct().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String integer) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        viewModel.getActAssets(integer);
                    }
                }).start();
            }
        });
        viewModel.activeListMutableLiveData.observe(this, new Observer<ActiveList>() {
            @Override
            public void onChanged(@Nullable ActiveList activeList) {
                if (activeList.getData().getActId().equals(viewModel.getCurrentAct().getValue())) {
                    if (activeList.getData().getActStatus().equals("NEW")) {
                        rankListAdapter.setNewData(new ArrayList<>());
                        recyclerView.setAdapter(rankListAdapter);
                        rankListAdapter.setCurrentActStatus(activeList.getData().getActStatus());
                        rankListAdapter.setCurrentActId(activeList.getData().getActId());
                    } else {
                        if (!activeList.getData().getActStatus().equals("START")) {
                            rankListAdapter.setNewData(activeList.getData().getVpnRanking());
                            recyclerView.setAdapter(rankListAdapter);
                            rankListAdapter.setCurrentActStatus(activeList.getData().getActStatus());
                            rankListAdapter.setCurrentActId(activeList.getData().getActId());
                        } else {
                            if (activeList.getData().getActStatus().equals("START")) {
                                List<ActiveList.DataBean.VpnRankingBean> list = activeList.getData().getVpnRanking();
                                ActiveList.DataBean.VpnRankingBean tempVpnRank = new ActiveList.DataBean.VpnRankingBean();
                                for (int i = 0; i < list.size(); i++) {
                                    if (list.get(i).getTotalQlc() < 50) {
                                        if (i == 0) {

                                        } else {
                                            list.add(i, tempVpnRank);
                                            newRankListAdapter.setFlagIndex(i);
                                        }
                                        break;
                                    }
                                }
                                newRankListAdapter.setNewData(list);
                                recyclerView.setAdapter(newRankListAdapter);
                                newRankListAdapter.setCurrentActStatus(activeList.getData().getActStatus());
                            } else {
                                newRankListAdapter.setNewData(activeList.getData().getVpnRanking());
                                recyclerView.setAdapter(newRankListAdapter);
                                newRankListAdapter.setCurrentActStatus(activeList.getData().getActStatus());
                            }
                        }
                    }
                } else {
                    rankListAdapter.setNewData(new ArrayList<>());
                    rankListAdapter.setCurrentActStatus("");
                    recyclerView.setAdapter(rankListAdapter);
                    rankListAdapter.setCurrentActId(activeList.getData().getActId());
                }
            }
        });
    }


    @Override
    protected void setupFragmentComponent() {
        DaggerRankListComponent
                .builder()
                .appComponent(((AppConfig) getActivity().getApplication()).getApplicationComponent())
                .rankListModule(new RankListModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void setPresenter(RankListContract.RankListContractPresenter presenter) {
        mPresenter = (RankListPresenter) presenter;
    }

    @Override
    protected void initDataFromLocal() {

    }

    @Override
    public void showProgressDialog() {
        progressDialog.show();
    }

    @Override
    public void closeProgressDialog() {
        progressDialog.hide();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}