import request from '@/utils/request'

export function add(data) {
  return request({
    url: 'api/camera',
    method: 'post',
    data
  })
}

export function del(id) {
  return request({
    url: 'api/camera/' + id,
    method: 'delete'
  })
}

export function edit(data) {
  return request({
    url: 'api/camera',
    method: 'put',
    data
  })
}

export function count(data) {
  return request({
    url: 'api/camera/count',
    method: 'get',
    data
  })
}

