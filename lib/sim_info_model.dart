class SIMInfoModel {
  String phoneNumber;
  String networkName;
  String operateName;

  SIMInfoModel({this.phoneNumber, this.networkName, this.operateName});

  factory SIMInfoModel.fromJson(Map<String, dynamic> json) {
    return SIMInfoModel(
      phoneNumber: json['phoneNumber'],
      networkName: json['networkName'],
      operateName: json['operateName'],
    );
  }
}
