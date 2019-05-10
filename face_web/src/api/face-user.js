import request from '@/utils/request'

export function add(data) {
  return request({
    url: 'api/face/users',
    method: 'post',
    data
  })
}

export function del(id) {
  return request({
    url: 'api/face/users/' + id,
    method: 'delete'
  })
}

export function edit(data) {
  return request({
    url: 'api/face/users',
    method: 'put',
    data
  })
}
export function count(data) {
  return request({
    url: 'api/face/count',
    method: 'get',
    data
  })
}
