package com.fehead.coredata.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fehead.coredata.controller.BaseController;
import com.fehead.coredata.controller.vo.TagDisplay;
import com.fehead.coredata.controller.vo.data.video.VideoDetailInfo;
import com.fehead.coredata.controller.vo.data.video.VideoDisplayInfo;
import com.fehead.coredata.controller.vo.data.video.VideoListDisplayInfo;
import com.fehead.coredata.dao.TagMapper;
import com.fehead.coredata.dao.VideoMapper;
import com.fehead.coredata.dao.dataobject.Tag;
import com.fehead.coredata.dao.dataobject.Video;
import com.fehead.coredata.error.BusinessException;
import com.fehead.coredata.error.EmBusinessError;
import com.fehead.coredata.inherent.DataType;
import com.fehead.coredata.service.VideoDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lmwis on 2019-08-12 09:42
 */
@Service
public class VideoDataServiceImpl extends BaseDataService implements VideoDataService{

    Logger logger = LoggerFactory.getLogger(BaseController.class);

    @Autowired
    VideoMapper videoMapper;

    @Autowired
    TagMapper tagMapper;

    /**
     * 查询详细video信息
     * @param id
     * @return
     * @throws BusinessException
     */
    @Override
    public VideoDetailInfo selectVideoModelById(Integer id) throws BusinessException {

        if(id==0){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        //从数据库获取所需资源

        Video video = new Video();
        try {
            video = videoMapper.selectById(id);
        } catch (Exception e) {
            logger.info("video查找失败");
            throw new BusinessException(EmBusinessError.DATA_SELECT_ERROR);
        }



        if(video==null){//资源不存在
            throw new BusinessException(EmBusinessError.DATA_RESOURCES_NOT_EXIST);
        }

        VideoDisplayInfo videoDisplayInfo = convertFromVideo(video);

        VideoDetailInfo videoDetailInfo = new VideoDetailInfo();

        BeanUtils.copyProperties(videoDisplayInfo, videoDetailInfo);

        videoDetailInfo.setVideo_url(video.getVideoPath());

        return videoDetailInfo;

    }

    @Override
    public VideoDisplayInfo findVideoDisplayInfoById(Integer id) throws BusinessException {
        return null;
    }

    /**
     * 查询video列表展示
     *  物理分页
     * @param pageable
     * @return
     */
    @Override
    public VideoListDisplayInfo selectVideoListsPageable(Pageable pageable) {

        if (pageable==null) return null;

        QueryWrapper<Video> wrapper = new QueryWrapper<>();

        // 默认按照创建时间进行排序，无修改必要
        wrapper.orderByDesc("datetime");
        // 分页设置
        Page<Video> page = new Page<>(pageable.getPageNumber(),pageable.getPageSize());
        // 分页结果
        IPage<Video> videoIPage = videoMapper.selectPage(page, wrapper);

        VideoListDisplayInfo lists = new VideoListDisplayInfo();

        List<VideoDisplayInfo> videoDisplayInfos = new ArrayList<>();
        videoIPage.getRecords().forEach(re->{
            // 获取标签信息
            List<Tag> tags = tagMapper.selectDataTagsByActualIdAndTypeId(re.getVideoId(),DataType.DATA_VIDEO.getId());
            List<TagDisplay> tagDisplays = convertFromTag(tags);

            VideoDisplayInfo videoDisplayInfo = convertFromVideo(re);
            videoDisplayInfo.setVideo_tags(tagDisplays);

            videoDisplayInfos.add(videoDisplayInfo);

        });


        // 最终封装
        lists.setVideo_lists(videoDisplayInfos);
        lists.setPage(pageable.getPageNumber());
        lists.setType_code(DataType.DATA_VIDEO.getCode());
        lists.setType_str(DataType.DATA_VIDEO.getStr());
        return lists;
    }

//    @Override
//    public VideoListDisplayInfo selectAllVideo() {
//
//        VideoListDisplayInfo lists = new VideoListDisplayInfo();
//
//        List<VideoDisplayInfo> videoDisplayInfos = new ArrayList<>();
//
//        videoMapper.selectList(null).forEach(re ->{
//            // 获取标签信息
//            List<Tag> tags = tagMapper.selectDataTagsByActualId(re.getVideoId());
//            List<TagDisplay> tagDisplays = convertFromTag(tags);
//
//            VideoDisplayInfo videoDisplayInfo = convertFromVideo(re);
//            videoDisplayInfo.setVideo_tags(tagDisplays);
//
//            videoDisplayInfos.add(videoDisplayInfo);
//        });
//        // 最终封装
//        lists.setVideo_lists(videoDisplayInfos);
//        lists.setPage(0);
//        lists.setType_code(DataType.DATA_VIDEO.getCode());
//        lists.setType_str(DataType.DATA_VIDEO.getStr());
//
//
//        return lists;
//    }

    /**
     * 将数据库的Video对象转换为表现层展示对象
     * @param video
     * @return
     */
    private VideoDisplayInfo convertFromVideo(Video video){
        if(video == null) return null;

        VideoDisplayInfo videoDisplayInfo = new VideoDisplayInfo();

        // 封装
        videoDisplayInfo.setId(video.getVideoId());
        videoDisplayInfo.setVideo_author(video.getAuthor());
        videoDisplayInfo.setVideo_des(video.getDes());
        videoDisplayInfo.setVideo_image(video.getImageUrl());
        videoDisplayInfo.setVideo_title(video.getTitle());
        videoDisplayInfo.setVideo_upload_time(video.getDatetime());


        return videoDisplayInfo;

    }


}
