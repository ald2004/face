import request from '@/utils/request'

export function add(data) {
  return request({
    url: 'api/userData',
    method: 'post',
    data
  })
}

export function del(id) {
  return request({
    url: 'api/userData/' + id,
    method: 'delete'
  })
}

export function edit(data) {
  return request({
    url: 'api/userData',
    method: 'put',
    data
  })
}

