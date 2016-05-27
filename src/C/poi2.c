#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>

#include <sys/socket.h>
#include <sys/types.h>
#include <netdb.h>
#include <netinet/in.h>
#include <netinet/udp.h>
#include <pthread.h>
#include "util1.h"

void *receive_udp(void *couple){
  int sock = ((struct sock_addr_couple *)couple)->sockfd;
  struct sockaddr_in adsock = ((struct sock_addr_couple *)couple)->adsock;
  char buff[100];
  while(1){
    int rec = recv(sock,buff,100,0);
    buff[rec] = '\0';
    printf("Received: %sFrom port: %d\n",buff, adsock.sin_port);
  }
}

int main(int argc, char *argv[]) {
  if (argc != 3) {
    printf("Passer deux ports en argument !\n");
    return -1;
  }
  
  // PARTIE TCP (pere)

  int pid = getpid();
  fork();
  if (pid) { // father
    int sock = socket(PF_INET, SOCK_STREAM, 0);
    struct sockaddr_in adsock;
    adsock.sin_family = AF_INET;
    adsock.sin_port = htons(atoi(argv[1]));
    bind(sock, (struct sockaddr *)&adsock, sizeof(struct sockaddr));
    listen(sock, 16); // TAILLE MAX DE LA FILE ARBITRAIRE
    socklen_t = rec_len;
    struct sockaddr rec_sock;
    accept(sock, (struct sockaddr *)&rec_sock, rec_len);
    char *buff[100];
    int rec = recv(sock, (void *)buff, 100, 0);
    

    return 0;
  }

  // PARTIE UDP (fils)

  int sock1 = socket(PF_INET,SOCK_DGRAM,0);
  struct sockaddr_in adsock1;
  adsock1.sin_family = AF_INET;
  adsock1.sin_port = htons(5555);
  adsock1.sin_addr.s_addr = htonl(INADDR_ANY);
  int sock2 = socket(PF_INET,SOCK_DGRAM,0);
  struct sockaddr_in adsock2;
  adsock2.sin_family = AF_INET;
  adsock2.sin_port = htons(5556);
  adsock2.sin_addr.s_addr = htonl(INADDR_ANY);
  struct sock_addr_couple couple1;
  couple1.sockfd = sock1;
  couple1.adsock = adsock1;
  struct sock_addr_couple couple2;
  couple2.sockfd = sock2;
  couple2.adsock = adsock2;
  int r = bind(sock1,(struct sockaddr *)&adsock1,sizeof(struct sockaddr_in));
  if(r == 0){
    int r2 = bind(sock2,(struct sockaddr *)&adsock2,sizeof(struct sockaddr_in));
    if(r2 == 0){
      pthread_t thr1,thr2;
      pthread_create(&thr1,NULL,receive_udp,(void *)&couple1);
      pthread_create(&thr2,NULL,receive_udp,(void *)&couple2);
      pthread_join(thr1,NULL);
      pthread_join(thr2,NULL);
    }
  }
  return 0;
}
