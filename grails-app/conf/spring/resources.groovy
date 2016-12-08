import org.springframework.security.oauth2.provider.token.DefaultTokenServices


// Place your Spring DSL code here
beans = {

  tokenServices(DefaultTokenServices){
    accessTokenValiditySeconds =  600;
    tokenStore = ref('tokenStore')
    supportRefreshToken = true;
    clientDetailsService = ref('clientDetailsService')
  }
}
