function loginApi(data) {
    return $axios({
      'url': '/user/login',
      'method': 'post',
      data
    })
  }

function loginoutApi() {
  return $axios({
    'url': '/user/loginout',
    'method': 'post',
  })
}


function sendmsgApi(data) {
    return $axios({
        'url': '/usr/code',
        'method': 'post',
        data
    })
}
  