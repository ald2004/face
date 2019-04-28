/*
created by L. 2018.05.16
*/

#include"detect.h"
#include <algorithm>
#include <map>
#include "det1.h"
#include "det2.h"
#include "det3.h"

namespace Face {

    Detect::Detect(const std::string &model_path) {

        std::vector<std::string> param_files = {
                model_path + "/det1.param",
                model_path + "/det2.param",
                model_path + "/det3.param"
        };

        std::vector<std::string> bin_files = {
                model_path + "/det1.bin",
                model_path + "/det2.bin",
                model_path + "/det3.bin"
        };

//        Pnet.load_param(param_files[0].data());
//        Pnet.load_model(bin_files[0].data());

#if NCNN_VULKAN
        Pnet.use_vulkan_compute = 1;
        Rnet.use_vulkan_compute = 1;
        Onet.use_vulkan_compute = 1;
#endif // NCNN_VULKAN
#if NCNN_VULKAN
        ncnn::create_gpu_instance();
#endif // NCNN_VULKAN

        Pnet.load_param_mem(DET1::param);
        Pnet.load_model(DET1::bin);
//        Rnet.load_param(param_files[1].data());
//        Rnet.load_model(bin_files[1].data());
        Rnet.load_param_mem(DET2::param);
        Rnet.load_model(DET2::bin);

//        Onet.load_param(param_files[2].data());
//        Onet.load_model(bin_files[2].data());
        Onet.load_param_mem(DET3::param);
        Onet.load_model(DET3::bin);


    }

    Detect::Detect(const std::vector<std::string> param_files, const std::vector<std::string> bin_files) {
        Pnet.load_param(param_files[0].data());
        Pnet.load_model(bin_files[0].data());
        Rnet.load_param(param_files[1].data());
        Rnet.load_model(bin_files[1].data());
        Onet.load_param(param_files[2].data());
        Onet.load_model(bin_files[2].data());
    }


    Detect::~Detect() {
        Pnet.clear();
        Rnet.clear();
        Onet.clear();
    }

    void Detect::SetMinFace(int minSize) {
        minsize = minSize;
    }

    void Detect::SetThreadNum(int threadNum) {
        threadnum = threadNum;
    }

    void Detect::generateBbox(ncnn::Mat score, ncnn::Mat location, std::vector<Bbox> &boundingBox_, float scale) {
        const int stride = 2;
        const int cellsize = 12;
        //score p
        float *p = score.channel(1);//score.data + score.cstep;
        //float *plocal = location.data;
        Bbox bbox{};
        float inv_scale = 1.0f / scale;
        for (int row = 0; row < score.h; row++) {
            for (int col = 0; col < score.w; col++) {
                if (*p > threshold[0]) {
                    bbox.score = *p;
                    bbox.x1 = static_cast<int>(round((stride * col + 1) * inv_scale));
                    bbox.y1 = static_cast<int>(round((stride * row + 1) * inv_scale));
                    bbox.x2 = static_cast<int>(round((stride * col + 1 + cellsize) * inv_scale));
                    bbox.y2 = static_cast<int>(round((stride * row + 1 + cellsize) * inv_scale));
                    bbox.area = (bbox.x2 - bbox.x1) * (bbox.y2 - bbox.y1);
                    const int index = row * score.w + col;
                    for (int channel = 0; channel < 4; channel++) {
                        bbox.regreCoord[channel] = location.channel(channel)[index];
                    }
                    boundingBox_.push_back(bbox);
                }
                p++;
                //plocal++;
            }
        }
    }

    void Detect::nms(std::vector<Bbox> &boundingBox_, const float overlap_threshold, std::string modelname) {
        if (boundingBox_.empty()) {
            return;
        }
        sort(boundingBox_.begin(), boundingBox_.end(), cmpScore);
        float IOU = 0;
        float maxX = 0;
        float maxY = 0;
        float minX = 0;
        float minY = 0;
        std::vector<int> vPick;
        unsigned int nPick = 0;
        std::multimap<float, int> vScores;
        const int num_boxes = boundingBox_.size();
        vPick.resize((unsigned int) num_boxes);
        for (int i = 0; i < num_boxes; ++i) {
            vScores.insert(std::pair<float, int>(boundingBox_[i].score, i));
        }
        while (!vScores.empty()) {
            unsigned int last = vScores.rbegin()->second;
            vPick[nPick] = last;
            nPick += 1;
            for (auto it = vScores.begin(); it != vScores.end();) {
                auto it_idx = static_cast<unsigned int>(it->second);
                maxX = (std::max)(boundingBox_.at(it_idx).x1, boundingBox_.at(last).x1);
                maxY = (std::max)(boundingBox_.at(it_idx).y1, boundingBox_.at(last).y1);
                minX = (std::min)(boundingBox_.at(it_idx).x2, boundingBox_.at(last).x2);
                minY = (std::min)(boundingBox_.at(it_idx).y2, boundingBox_.at(last).y2);
                //maxX1 and maxY1 reuse
                maxX = ((minX - maxX + 1) > 0) ? (minX - maxX + 1) : 0;
                maxY = ((minY - maxY + 1) > 0) ? (minY - maxY + 1) : 0;
                //IOU reuse for the area of two bbox
                IOU = maxX * maxY;
                if (!modelname.compare("Union"))
                    IOU = IOU / (boundingBox_.at(it_idx).area + boundingBox_.at(last).area - IOU);
                else if (!modelname.compare("Min")) {
                    IOU = IOU /
                          ((boundingBox_.at(it_idx).area < boundingBox_.at(last).area) ? boundingBox_.at(it_idx).area
                                                                                       : boundingBox_.at(last).area);
                }
                if (IOU > overlap_threshold) {
                    it = vScores.erase(it);
                } else {
                    it++;
                }
            }
        }

        vPick.resize(nPick);
        std::vector<Bbox> tmp_;
        tmp_.resize(nPick);
        for (unsigned int i = 0; i < nPick; i++) {
            tmp_[i] = boundingBox_[vPick[i]];
        }
        boundingBox_ = tmp_;
    }

    void Detect::refine(std::vector<Bbox> &vecBbox, const int &height, const int &width, bool square) {
        if (vecBbox.empty()) {
            std::cout << "Bbox is empty!!" << std::endl;
            return;
        }
        float bbw = 0, bbh = 0, maxSide = 0;
        float h = 0, w = 0;
        float x1 = 0, y1 = 0, x2 = 0, y2 = 0;
        for (auto &it : vecBbox) {
            bbw = it.x2 - it.x1 + 1;
            bbh = it.y2 - it.y1 + 1;
            x1 = it.x1 + it.regreCoord[0] * bbw;
            y1 = it.y1 + it.regreCoord[1] * bbh;
            x2 = it.x2 + it.regreCoord[2] * bbw;
            y2 = it.y2 + it.regreCoord[3] * bbh;

            if (square) {
                w = x2 - x1 + 1;
                h = y2 - y1 + 1;
                maxSide = (h > w) ? h : w;
                x1 = static_cast<float>(x1 + w * 0.5 - maxSide * 0.5);
                y1 = static_cast<float>(y1 + h * 0.5 - maxSide * 0.5);
                it.x2 = static_cast<int>(round(x1 + maxSide - 1));
                it.y2 = static_cast<int>(round(y1 + maxSide - 1));
                it.x1 = static_cast<int>(round(x1));
                it.y1 = static_cast<int>(round(y1));
            }

            //boundary check
            if (it.x1 < 0)it.x1 = 0;
            if (it.y1 < 0)it.y1 = 0;
            if (it.x2 > width)it.x2 = width - 1;
            if (it.y2 > height)it.y2 = height - 1;

            it.area = (it.x2 - it.x1) * (it.y2 - it.y1);
        }
    }

    void Detect::PNet() {
        firstBbox_.clear();
        float minl = img_w < img_h ? img_w : img_h;
        float m = (float) MIN_DET_SIZE / minsize;
        minl *= m;
        float factor = pre_facetor;
        std::vector<float> scales_;
        while (minl > MIN_DET_SIZE) {
            scales_.push_back(m);
            minl *= factor;
            m = m * factor;
        }
        for (float scale : scales_) {
            int hs = (int) ceil(img_h * scale);
            int ws = (int) ceil(img_w * scale);
            ncnn::Mat in;
            resize_bilinear(img, in, ws, hs);
            ncnn::Extractor ex = Pnet.create_extractor();
            ex.set_num_threads(threadnum);
            ex.set_light_mode(true);
            ex.input("data", in);
            ncnn::Mat score_, location_;
            ex.extract("prob1", score_);
            ex.extract("conv4-2", location_);
            std::vector<Bbox> boundingBox_;
            generateBbox(score_, location_, boundingBox_, scale);
            nms(boundingBox_, nms_threshold[0]);
            firstBbox_.insert(firstBbox_.end(), boundingBox_.begin(), boundingBox_.end());
            boundingBox_.clear();
        }
    }

    std::vector<Bbox> Detect::PNet(ncnn::Mat &img) {
        std::vector<Bbox> boxes;
        int img_w = img.w;
        int img_h = img.h;
        float minl = img_w < img_h ? img_w : img_h;
        float m = (float) MIN_DET_SIZE / minsize;
        minl *= m;
        float factor = pre_facetor;
        std::vector<float> scales_;
        while (minl > MIN_DET_SIZE) {
            scales_.push_back(m);
            minl *= factor;
            m = m * factor;
        }
        for (float scale : scales_) {
            int hs = (int) ceil(img_h * scale);
            int ws = (int) ceil(img_w * scale);
            ncnn::Mat in;
            resize_bilinear(img, in, ws, hs);
            ncnn::Extractor ex = Pnet.create_extractor();
            ex.set_num_threads(threadnum);
            ex.set_light_mode(true);
            ex.input("data", in);
            ncnn::Mat score_, location_;
            ex.extract("prob1", score_);
            ex.extract("conv4-2", location_);
            std::vector<Bbox> boundingBox_;
            generateBbox(score_, location_, boundingBox_, scale);
            nms(boundingBox_, nms_threshold[0]);
            boxes.insert(boxes.end(), boundingBox_.begin(), boundingBox_.end());
            boundingBox_.clear();
        }
        return boxes;
    }


    void Detect::RNet() {
        secondBbox_.clear();
        int count = 0;
        for (auto &it : firstBbox_) {
            ncnn::Mat tempIm;
            copy_cut_border(img, tempIm, it.y1, img_h - it.y2, it.x1, img_w - it.x2);
            ncnn::Mat in;
//            std::cout << tempIm.w << "," << tempIm.h << std::endl;
            resize_bilinear(tempIm, in, 24, 24);
            ncnn::Extractor ex = Rnet.create_extractor();
            ex.set_num_threads(threadnum);
            ex.set_light_mode(true);
            ex.input("data", in);
            ncnn::Mat score, bbox;
            ex.extract("prob1", score);
            ex.extract("conv5-2", bbox);

            if ((float) score[1] > threshold[1]) {
                for (int channel = 0; channel < 4; channel++) {
                    it.regreCoord[channel] = (float) bbox[channel];//*(bbox.data+channel*bbox.cstep);
                }
                it.area = (it.x2 - it.x1) * (it.y2 - it.y1);
                it.score = score.channel(1)[0];//*(score.data+score.cstep);
                secondBbox_.push_back(it);
            }
        }
    }

    /**
     *
     * @param resize_src 24*24
     * @param it  x2 = src.width  , y2 = src.height
     * @return
     */
    std::vector<Bbox> Detect::RNet(ncnn::Mat &resize_src, Bbox it) {
        std::vector<Bbox> boxes;
        ncnn::Mat in;
        resize_bilinear(resize_src, in, 48, 48);
        ncnn::Extractor ex = Rnet.create_extractor();
        ex.set_num_threads(threadnum);
        ex.set_light_mode(true);
        ex.input("data", in);
        ncnn::Mat score, bbox;
        ex.extract("prob1", score);
        ex.extract("conv5-2", bbox);

        if ((float) score[1] > threshold[1]) {
            for (int channel = 0; channel < 4; channel++) {
                it.regreCoord[channel] = (float) bbox[channel];//*(bbox.data+channel*bbox.cstep);
            }
            it.area = (it.x2 - it.x1) * (it.y2 - it.y1);
            it.score = score.channel(1)[0];//*(score.data+score.cstep);
            boxes.push_back(it);
        }
        return boxes;
    }

    void Detect::ONet() {
        thirdBbox_.clear();
        for (auto &it : secondBbox_) {
            ncnn::Mat tempIm;
            copy_cut_border(img, tempIm, it.y1, img_h - it.y2, it.x1, img_w - it.x2);
            ncnn::Mat in;
            resize_bilinear(tempIm, in, 48, 48);
            ncnn::Extractor ex = Onet.create_extractor();
            ex.set_num_threads(threadnum);
            ex.set_light_mode(true);
            ex.input("data", in);
            ncnn::Mat score, bbox, keyPoint;
            ex.extract("prob1", score);
            ex.extract("conv6-2", bbox);
            ex.extract("conv6-3", keyPoint);
            if ((float) score[1] > threshold[2]) {
                for (int channel = 0; channel < 4; channel++) {
                    it.regreCoord[channel] = (float) bbox[channel];
                }
                it.area = (it.x2 - it.x1) * (it.y2 - it.y1);
                it.score = score.channel(1)[0];
                for (int num = 0; num < 5; num++) {
                    (it.ppoint)[num] = it.x1 + (it.x2 - it.x1) * keyPoint[num];
                    (it.ppoint)[num + 5] = it.y1 + (it.y2 - it.y1) * keyPoint[num + 5];
                }

                thirdBbox_.push_back(it);
            }
        }
    }

    /**
     *
     * @param resize_src 48 * 48
     * @return
     */
    std::vector<Bbox> Detect::ONet(ncnn::Mat &resize_src, Bbox it) {
        std::vector<Bbox> boxes;
        ncnn::Extractor ex = Onet.create_extractor();
        ex.set_num_threads(threadnum);
        ex.set_light_mode(true);
        ex.input("data", resize_src);
        ncnn::Mat score, bbox, keyPoint;
        ex.extract("prob1", score);
        ex.extract("conv6-2", bbox);
        ex.extract("conv6-3", keyPoint);
        if ((float) score[1] > threshold[2]) {
            for (int channel = 0; channel < 4; channel++) {
                it.regreCoord[channel] = (float) bbox[channel];
            }
            it.area = (it.x2 - it.x1) * (it.y2 - it.y1);
            it.score = score.channel(1)[0];
            for (int num = 0; num < 5; num++) {
                (it.ppoint)[num] = it.x1 + (it.x2 - it.x1) * keyPoint[num];
                (it.ppoint)[num + 5] = it.y1 + (it.y2 - it.y1) * keyPoint[num + 5];
            }

            boxes.push_back(it);
        }

        return boxes;
    }


    void Detect::start(const ncnn::Mat &ncnn_img, std::vector<Bbox> &finalBbox_) {
        img = ncnn_img;
        img_w = img.w;
        img_h = img.h;
        img.substract_mean_normalize(mean_vals, norm_vals);


        PNet();
        //the first stage's nms
        if (firstBbox_.empty()) return;
        nms(firstBbox_, nms_threshold[0]);
        refine(firstBbox_, img_h, img_w, true);
//        printf("firstBbox_.size()=%zd\n", firstBbox_.size());

        //second stage
        RNet();
//        printf("secondBbox_.size()=%zd\n", secondBbox_.size());
        if (secondBbox_.empty()) return;
        nms(secondBbox_, nms_threshold[1]);
        refine(secondBbox_, img_h, img_w, true);

        //third stage
        ONet();



//        printf("thirdBbox_.size()=%zd\n", thirdBbox_.size());
        if (thirdBbox_.empty()) return;
        refine(thirdBbox_, img_h, img_w, true);
        nms(thirdBbox_, nms_threshold[2], "Min");
        finalBbox_ = thirdBbox_;
    }

    void Detect::start2(const ncnn::Mat &ncnn_img,
                        std::vector<Bbox> &finalBbox1_,
                        std::vector<Bbox> &finalBbox2_,
                        std::vector<Bbox> &finalBbox3_) {
        img = ncnn_img;
        img_w = img.w;
        img_h = img.h;
        img.substract_mean_normalize(mean_vals, norm_vals);

        PNet();
        //the first stage's nms
        if (firstBbox_.empty()) return;
        nms(firstBbox_, nms_threshold[0]);
        refine(firstBbox_, img_h, img_w, true);
//        printf("firstBbox_.size()=%zd\n", firstBbox_.size());

        //second stage
        RNet();
//        printf("secondBbox_.size()=%zd\n", secondBbox_.size());
        if (secondBbox_.empty()) return;
        nms(secondBbox_, nms_threshold[1]);
        refine(secondBbox_, img_h, img_w, true);

        //third stage
        ONet();
//        printf("thirdBbox_.size()=%zd\n", thirdBbox_.size());
        if (thirdBbox_.empty()) return;
        refine(thirdBbox_, img_h, img_w, true);
        nms(thirdBbox_, nms_threshold[2], "Min");
        finalBbox1_ = firstBbox_;
        finalBbox2_ = secondBbox_;
        finalBbox3_ = thirdBbox_;
    }


    void Detect::setThreshold(float *threshold) {
        memcpy(this->threshold, threshold, sizeof(this->threshold));
    }

    bool cmpScore(Bbox lsh, Bbox rsh) {
        return lsh.score < rsh.score;
    }
}