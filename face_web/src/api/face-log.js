import request from '@/utils/request'

export function del(id) {
  return request({
    url: 'api/faceLog/logs/' + id,
    method: 'delete'
  })
}

export function get() {
  return request({
    url: 'api/faceLog/count/camera/top/10',
    method: 'get'
  })
}

export function getnum(id) {
  return request({
    url: 'api/faceLog/countNew/'+id,
    method: 'get'
  })
}

