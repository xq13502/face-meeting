import request from '@/utils/request'
export function signStatus(query) {
    return request({
        url: '/qrcode/signStatus',
        method: 'get',
        params: query
    })
}

export function generateCheckToken(data) {
    return request({
        url: '/qrcode/generateCheckToken',
        method: 'post',
        data: data
    })
}

export function verifyCheckToken(data) {
    return request({
        url: '/qrcode/verifyCheckToken',
        method: 'post',
        data: data
    })
}

export function confirmSign(data) {
    return request({
        url: '/qrcode/confirmSign',
        method: 'post',
        data: data
    })
}