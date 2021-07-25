FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1

RUN  apt-get install -y make

WORKDIR /exchange_platform
COPY  . /exchange_platform/
RUN chmod 777 /exchange_platform/run_services.sh
RUN sbt assembly
EXPOSE 9994
CMD /exchange_platform/run_services.sh
