function fn() {
  var env = karate.env || 'local';

  var config = {
    env: env,
    baseUrl: karate.properties['karate.baseUrl'] || 'http://localhost:8082',
    connectTimeout: 12000,
    readTimeout: 12000,
    defaultPassword: karate.properties['karate.defaultPassword'] || 'Test@123456',
    platformEmail: karate.properties['karate.platformEmail'] || 'admin@sanchalak.in',
    platformPassword: karate.properties['karate.platformPassword'] || 'password'
  };

  if (env == 'ci') {
    config.baseUrl = karate.properties['karate.baseUrl'] || 'http://localhost:8082';
  }

  if (env == 'staging') {
    config.baseUrl = karate.properties['karate.baseUrl'] || 'http://localhost:8082';
  }

  karate.configure('connectTimeout', config.connectTimeout);
  karate.configure('readTimeout', config.readTimeout);

  return config;
}
