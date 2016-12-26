package com.example.root.experimentassistant.FirstLevel;

import com.example.root.experimentassistant.Adapter.CoursesAdapter;
import com.example.root.experimentassistant.Model.*;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.cjj.MaterialRefreshLayout;
import com.cjj.MaterialRefreshListener;
import com.example.root.experimentassistant.MyView.*;
import com.example.root.experimentassistant.R;
import com.example.root.experimentassistant.ViewModel.ViewCourse;

import java.util.List;

/**
 * Created by root on 2016/12/6.
 */
public class CourseList extends Fragment implements MySearchViewListener{
    private SearchView searchView;

    private ListView courseList;

    private MaterialRefreshLayout myMaterialRefreshLayout;

    private Courses courses;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view =inflater.inflate(R.layout.course_list,container,false);

        searchView=(SearchView) view.findViewById(R.id.search_view);
        courseList=(ListView)view.findViewById(R.id.course_list);
        myMaterialRefreshLayout=(MaterialRefreshLayout)view.findViewById(R.id.course_refresh);
        courses=new Courses(this.getContext());

        searchView.bindListener(this);
//        courseList.setAdapter(new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,courses));

        //刷新回调函数
        myMaterialRefreshLayout.setMaterialRefreshListener(new MaterialRefreshListener() {
            @Override
            public void onRefresh(final MaterialRefreshLayout materialRefreshLayout) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myMaterialRefreshLayout.finishRefresh();
                    }
                },3000);
            }
            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout) {
                super.onRefreshLoadMore(materialRefreshLayout);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        myMaterialRefreshLayout.finishRefreshLoadMore();
                    }
                },3000);
            }
        });

        //获取课程数据
        courses.getDefault(new RequestCallBack() {
            @Override
            public void onRequestSuccess(Object sender) {
                List<ViewCourse> viewCourses=(List<ViewCourse>)sender;
                CoursesAdapter adapter=new CoursesAdapter(getContext(),viewCourses);
                courseList.setAdapter(adapter);
            }

            @Override
            public void onRequestFailure(Object sender) {

            }
        });

        return view;
    }

    @Override
    public void search(String searchText){
        courses.search(searchText, new RequestCallBack() {
            @Override
            public void onRequestSuccess(Object sender) {
                List<ViewCourse> viewCourses= (List<ViewCourse>)sender;
                CoursesAdapter adapter=new CoursesAdapter(getContext(),viewCourses);
                courseList.setAdapter(adapter);
            }

            @Override
            public void onRequestFailure(Object sender) {

            }
        });
    }

    @Override
    public void getMatching(String matchText){
//        List<String> suggest=courses.getSuggest(matchText);
//        return new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,suggest.toArray(new String[suggest.size()]));
        courses.getSuggest(matchText, new RequestCallBack() {
            @Override
            public void onRequestSuccess(Object sender) {
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(CourseList.this.getContext(),R.layout.course_entry,(List<String>)sender);
                searchView.setSuggestList(adapter);
            }

            @Override
            public void onRequestFailure(Object sender) {

            }
        });
    }
}