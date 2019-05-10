const baseUrl = process.env.BASE_API
const api = {
  state: {
    // 实时控制台
    socketApi: baseUrl + '/websocket?token=kl',
    // 图片上传
    imagesUploadApi: baseUrl + '/api/pictures',
    // 上传正面照
    uploadFacePicApi: baseUrl + '/api/face/upload/pictures',
    // 批量上传
    uploadFaceUsersPicApi: baseUrl + '/api/face/upload/faceUsers',
    // 修改头像
    updateAvatarApi: baseUrl + '/api/users/updateAvatar',
    // 上传文件到七牛云
    qiNiuUploadApi: baseUrl + '/api/qiNiuContent',

    findmec: baseUrl + '/api/findMes',

  }
}

export default api
