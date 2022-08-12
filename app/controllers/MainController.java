package controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import jwt.JwtControllerHelper;
import model.Appointment;
import model.PatientParams;
import model.ScheduleParams;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.api.Play;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController extends Controller {
    Long sisakuotajkn;
    Long kuotajkn;
    Long sisakuotanonjkn;
    Long kuotanonjkn;
    Long estimasidilayani;
    Long angkaantrean;
    Boolean tokenVerified = false;
    Boolean tokenExpired = false;
    private ObjectMapper mapper;
    private Http.Context ctx;
    @Inject
    private JwtControllerHelper jwtControllerHelper;
    @Inject
    private Config config;
    private JWTVerifier verifier;

    private String getFlexGwBaseUrl() {
        String url = config.getString("integration.flexgw.baseurl");
        return url;
    }

    private String getFlexGwTokenUrl() {
        String url = config.getString("integration.flexgw.auth.token_url");
        return url;
    }

    private String getProvider() {
        String provider = config.getString("integration.flexgw.auth.provider");
        return provider;
    }

    private String getProviderPerujuk() {
        String providerPerujuk = config.getString("integration.flexgw.auth.provider_perujuk");
        return providerPerujuk;
    }



    /**
     * Pasien Baru
     * Fungsi : Informasi identitas pasien baru yang belum punya rekam medis (tidak ada norm di Aplikasi VClaim)
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result pasienBaru() throws UnsupportedEncodingException {
        ObjectNode response = Json.newObject();
        ObjectNode metadata = Json.newObject();
        ObjectNode result = Json.newObject();

        verifToken();
        try {
            if (tokenVerified) {
                JsonNode jsonNode = request().body().asJson();
                String nomorkartu = StringUtils.isBlank(jsonNode.get("nomorkartu").asText()) ? null : jsonNode.get("nomorkartu").asText();
                String nik = StringUtils.isBlank(jsonNode.get("nik").asText()) ? null : jsonNode.get("nik").asText();
                String nomorkk = StringUtils.isBlank(jsonNode.get("nomorkk").asText()) ? null : jsonNode.get("nomorkk").asText();
                String nama = StringUtils.isBlank(jsonNode.get("nama").asText()) ? null : jsonNode.get("nama").asText();
                String jeniskelamin = StringUtils.isBlank(jsonNode.get("jeniskelamin").asText()) ? null : jsonNode.get("jeniskelamin").asText();
                String tanggallahir = StringUtils.isBlank(jsonNode.get("tanggallahir").asText()) ? null : jsonNode.get("tanggallahir").asText();
                String nohp = StringUtils.isBlank(jsonNode.get("nohp").asText()) ? null : jsonNode.get("nohp").asText();
                String alamat = StringUtils.isBlank(jsonNode.get("alamat").asText()) ? null : jsonNode.get("alamat").asText();
                String kodeprop = StringUtils.isBlank(jsonNode.get("kodeprop").asText()) ? null : jsonNode.get("kodeprop").asText();
                String namaprop = StringUtils.isBlank(jsonNode.get("namaprop").asText()) ? null : jsonNode.get("namaprop").asText();
                String kodedati2 = StringUtils.isBlank(jsonNode.get("kodedati2").asText()) ? null : jsonNode.get("kodedati2").asText();
                String namadati2 = StringUtils.isBlank(jsonNode.get("namadati2").asText()) ? null : jsonNode.get("namadati2").asText();
                String kodekec = StringUtils.isBlank(jsonNode.get("kodekec").asText()) ? null : jsonNode.get("kodekec").asText();
                String namakec = StringUtils.isBlank(jsonNode.get("namakec").asText()) ? null : jsonNode.get("namakec").asText();
                String kodekel = StringUtils.isBlank(jsonNode.get("kodekel").asText()) ? null : jsonNode.get("kodekel").asText();
                String namakel = StringUtils.isBlank(jsonNode.get("namakel").asText()) ? null : jsonNode.get("namakel").asText();
                String rw = StringUtils.isBlank(jsonNode.get("rw").asText()) ? null : jsonNode.get("rw").asText();
                String rt = StringUtils.isBlank(jsonNode.get("rt").asText()) ? null : jsonNode.get("rt").asText();

                try {

                    // request token flexGW
                    if (oauth() != null) {
                        try {

                            if (nomorkartu == null) {
                                metadata.put("message", "Nomor Kartu Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (nik == null) {
                                metadata.put("message", "NIK Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (nomorkk == null) {
                                metadata.put("message", "Nomor KK Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (nama == null) {
                                metadata.put("message", "Nama Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (jeniskelamin == null) {
                                metadata.put("message", "Jenis Kelamin Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (tanggallahir == null) {
                                metadata.put("message", "Tanggal Lahir Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (alamat == null) {
                                metadata.put("message", "Alamat Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (kodeprop == null) {
                                metadata.put("message", "Kode Propinsi Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (namaprop == null) {
                                metadata.put("message", "Nama Propinsi Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (kodedati2 == null) {
                                metadata.put("message", "Kode Dati 2 Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (namadati2 == null) {
                                metadata.put("message", "Nama Dati 2 Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (kodekec == null) {
                                metadata.put("message", "Kode Kecamatan Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (namakec == null) {
                                metadata.put("message", "Nama Kecamatan Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (kodekel == null) {
                                metadata.put("message", "Kode Kelurahan Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (namakel == null) {
                                metadata.put("message", "Nama Kelurahan Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (rt == null) {
                                metadata.put("message", "RT Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (rw == null) {
                                metadata.put("message", "RW Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            if (namakel == null) {
                                metadata.put("message", "Nama Kelurahan Belum Diisi").put("code", 201);
                                return ok(metadata);
                            }

                            // Format Nomor Kartu hanya numeric dan 13 digit
                            for (int i = 0; i < nomorkartu.length(); i++) {
                                if (nomorkartu.charAt(i) >= '0' && nomorkartu.charAt(i) <= '9' && nomorkartu.length() == 13){

                                }else{
                                    metadata.put("message", "Format Nomor Kartu Tidak Sesuai").put("code", 201);
                                    return ok(metadata);
                                }
                            }

                            // Format NIK hanya numeric dan 16 digit
                            for (int i = 0; i < nik.length(); i++) {
                                if (nik.charAt(i) >= '0' && nik.charAt(i) <= '9' && nik.length() == 16){

                                }else{
                                    metadata.put("message", "Format NIK Tidak Sesuai").put("code", 201);
                                    return ok(metadata);
                                }
                            }

                            if (!validBirtDate(tanggallahir)){
                                metadata.put("message", "Format Tanggal Lahir Tidak Sesuai").put("code", 201);
                                return ok(metadata);
                            }

                            PatientParams param = new PatientParams(nomorkartu, nik, nomorkk, nama, jeniskelamin, tanggallahir, nohp, alamat, kodeprop, namaprop, kodedati2, namadati2, kodekec, namakec, kodekel, namakel, rw, rt);
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode appointmentNode = mapper.valueToTree(param);
                            WSClient ws = Play.current().injector().instanceOf(WSClient.class);
                            CompletionStage<JsonNode> responseWS = ws.url(getFlexGwBaseUrl() + "/bpjs/patients")
                                    .addHeader("Authorization", "Bearer " + oauth())
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("X-Provider-Code", getProvider())
                                    .addHeader("X-Dev-Provider-Code", getProvider())
                                    .setContentType("application/x-www-form-urlencoded")
                                    .post(appointmentNode)
                                    .thenApply(wsResponse -> wsResponse.asJson());
                            CompletableFuture<JsonNode> node = responseWS.toCompletableFuture();
                            JsonNode jsonNodes = node.get();
                            Map<String, Object> data = mapper.convertValue(jsonNodes, Map.class);
                            try {
                                if (StringUtils.isNotBlank(data.get("id").toString())) {
                                    response.put("norm", data.get("mrNo").toString());
                                    metadata.put("message", "Harap datang ke admisi untuk melengkapi data rekam medis").put("code", 200);
                                    result.put("response", response);
                                    result.put("metadata", metadata);
                                    return ok(result);
                                }
                            } catch (NullPointerException e) {
                                Logger.error(e.getClass().getName() + " -> " + "A.1");
                                metadata.put("message", "Data Peserta Sudah Pernah Dientrikan").put("code", 201);
                                return ok(metadata);
                            }

                            try {
                                if (StringUtils.isNotBlank(data.get("code").toString())) {
                                    metadata.put("message", "Gagal").put("code", 201);
                                    return ok(metadata);
                                }
                            } catch (NullPointerException e) {
                                Logger.error(e.getClass().getName() + " -> " + "A.2");
                            }

                        } catch (UnsupportedEncodingException e) {
                            Logger.error(e.getMessage());
                        } catch (Exception e) {
                            Logger.error(e.getClass().getName() + " -> " + e.getMessage());
                            metadata.put("message", "Gagal").put("code", 201);
                            return ok(metadata);
                        }
                    } else {
                        metadata.put("message", "Gagal").put("code", 201);
                        return ok(metadata);
                    }

                } catch (Exception e) {
                    Logger.error(this.getClass().getName(), e);
                }
                metadata.put("message", "Sukses").put("code", 200);
                return ok(metadata);
            } else {
                metadata.put("message", "Gagal").put("code", 201);
                return ok(metadata);
            }
        } catch (NullPointerException e) {
            metadata.put("message", "Gagal").put("code", 201);
            return ok(metadata);
        }
    }

    /**
     * Ambil Antrean
     * Fungsi : Mengambil antrean
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result ambilAntrian() throws UnsupportedEncodingException {
        ObjectNode response = Json.newObject();
        ObjectNode metadata = Json.newObject();
        ObjectNode result = Json.newObject();
        String prefix = config.getString("prefix");

        verifToken();
        try {
            if (tokenVerified) {
                JsonNode jsonNode = request().body().asJson();
                String nomorkartu = StringUtils.isBlank(jsonNode.get("nomorkartu").asText()) ? null : jsonNode.get("nomorkartu").asText();
                String nik = StringUtils.isBlank(jsonNode.get("nik").asText()) ? null : jsonNode.get("nik").asText();
                String nohp = StringUtils.isBlank(jsonNode.get("nohp").asText()) ? null : jsonNode.get("nohp").asText();
                String kodepoli = StringUtils.isBlank(jsonNode.get("kodepoli").asText()) ? null : jsonNode.get("kodepoli").asText();
                String norm = StringUtils.isBlank(jsonNode.get("norm").asText()) ? null : jsonNode.get("norm").asText();
                String tanggalperiksa = StringUtils.isBlank(jsonNode.get("tanggalperiksa").asText()) ? null : jsonNode.get("tanggalperiksa").asText();
                Long kodedokter = Long.parseLong(jsonNode.get("kodedokter").asText());

                String jampraktek = StringUtils.isBlank(jsonNode.get("jampraktek").asText()) ? null : jsonNode.get("jampraktek").asText();
                String[] arrOfJampraktek = jampraktek.split("-");
                String jampraktekbuka = arrOfJampraktek[0];
                String jampraktektutup = arrOfJampraktek[1];

                Long jeniskunjungan = Long.parseLong(jsonNode.get("jeniskunjungan").asText());
                String nomorreferensi = StringUtils.isBlank(jsonNode.get("nomorreferensi").asText()) ? null : jsonNode.get("nomorreferensi").asText();

                try {
                    ScheduleParams param = new ScheduleParams(tanggalperiksa, tanggalperiksa, kodepoli, String.valueOf(kodedokter), jampraktekbuka, jampraktektutup);
                    Long scheduleId = scheduleId(param);

                    // request token flexhis-gateway
                    if (oauth() != null && scheduleId != null) {
                        try {

                            Appointment appointment = new Appointment();
                            appointment.setMrNo(norm);
                            appointment.setBpjsNo(nomorkartu);
                            appointment.setScheduleCode(String.valueOf(scheduleId));
                            appointment.setNoRujukan(nomorreferensi);
                            appointment.setProviderPerujuk(getProviderPerujuk());

                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode appointmentNode = mapper.valueToTree(appointment);
                            WSClient ws = Play.current().injector().instanceOf(WSClient.class);
                            CompletionStage<JsonNode> responseWS = ws.url(getFlexGwBaseUrl() + "/bpjs/appointments")
                                    .addHeader("Authorization", "Bearer " + oauth())
                                    .addHeader("Content-Type", "application/json")
                                    .addHeader("X-Provider-Code", getProvider())
                                    .setContentType("application/x-www-form-urlencoded")
                                    .post(appointmentNode)
                                    .thenApply(wsResponse -> wsResponse.asJson());
                            CompletableFuture<JsonNode> node = responseWS.toCompletableFuture();
                            JsonNode jsonNodes = node.get();
                            Logger.debug(jsonNodes.toString());
                            try {
                                if (jsonNodes.get("code").asText().matches("GWAPT020")) {
                                    metadata.put("message", "Nomor Antrean Hanya Dapat Diambil 1 Kali Pada Tanggal Yang Sama").put("code", 201);
                                    return ok(metadata);
                                }
                                if (jsonNodes.get("code").asText().matches("GWAPT020")) {
                                    metadata.put("message", "Pendaftaran ke Poli Ini Sedang Tutup").put("code", 201);
                                    return ok(metadata);
                                }
                                if (jsonNodes.get("code").asText().matches("GWBDR002")) {
                                    metadata.put("message", "Gagal").put("code", 201);
                                    return ok(metadata);
                                }
                                if (jsonNodes.get("code").asText().matches("GWAPT025")) {
                                    metadata.put("message", "Jadwal yang dipilih melebihi masa aktif rujukan.").put("code", 201);
                                    return ok(metadata);
                                }
                                if (jsonNodes.get("code").asText().matches("GWAPT024")) {
                                    metadata.put("message", jsonNodes.get("message").asText()).put("code", 201);
                                    return ok(metadata);
                                }

                            } catch (NullPointerException e) {
                                Map<String, Object> data = mapper.convertValue(jsonNodes, Map.class);
                                Map<String, Object> visitInformation = mapper.convertValue(jsonNodes.get("visitInformation"), Map.class);
                                Map<String, Object> doctor = mapper.convertValue(jsonNodes.get("doctor"), Map.class);
                                response.put("nomorantrean", prefix + "-" + angkaantrean).put("angkaantrean", angkaantrean).put("kodebooking", data.get("appointmentNo").toString()).put("norm", data.get("mrNo").toString()).put("namapoli", visitInformation.get("polyclinic").toString())
                                        .put("namadokter", doctor.get("name").toString()).put("estimasidilayani", estimasidilayani).put("sisakuotajkn", sisakuotajkn - 1).put("kuotajkn", kuotajkn)
                                        .put("sisakuotanonjkn", sisakuotanonjkn - 1).put("kuotanonjkn", kuotanonjkn).put("keterangan", "Peserta harap 60 menit lebih awal guna pencatatan administrasi.");
                                metadata.put("message", "Ok").put("code", 200);
                                result.put("response", response);
                                result.put("metadata", metadata);
                                return ok(result);
                            }

                        } catch (UnsupportedEncodingException e) {
                            Logger.error(e.getMessage());
                        } catch (Exception e) {
                            //pasien baru
                            Logger.error(e.getMessage());
                            metadata.put("message", "Data pasien ini tidak ditemukan, silahkan Melakukan Registrasi Pasien Baru").put("code", 202);
                            return ok(metadata);
                        }
                    } else {
                        metadata.put("message", "Gagal, Jadwal Belum Tersedia, Silahkan Reschedule Tanggal dan Jam Praktek Lainnya").put("code", 201);
                        return ok(metadata);
                    }

                } catch (Exception e) {
                    Logger.error(this.getClass().getName(), e);
                }
                metadata.put("message", "Sukses").put("code", 200);
                return ok(metadata);
            } else {
                metadata.put("message", "Gagal, Token Expired").put("code", 201);
                return ok(metadata);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            metadata.put("message", "Gagal").put("code", 201);
            return ok(metadata);
        }

    }


    /**
     * Check In
     * Fungsi : Memastikan pasien sudah datang di RS
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result checkin() throws UnsupportedEncodingException {
        ObjectNode metadata = Json.newObject();
        ObjectNode result = Json.newObject();

        verifToken();
        try {
            if (tokenVerified) {
                JsonNode jsonNode = request().body().asJson();
                String kodebooking = StringUtils.isBlank(jsonNode.get("kodebooking").asText()) ? null : jsonNode.get("kodebooking").asText();
                String waktu = StringUtils.isBlank(jsonNode.get("waktu").asText()) ? null : jsonNode.get("waktu").asText();
                metadata.put("message", "Ok").put("code", 200);
                result.put("metadata", metadata);
                return ok(result);

            } else {
                metadata.put("message", "Gagal").put("code", 201);
                return ok(metadata);
            }
        } catch (NullPointerException e) {
            metadata.put("message", "Gagal").put("code", 201);
            return ok(metadata);
        }

    }


    /**
     * Status Antrean
     * Fungsi : Menampilkan status antrean per poli (digunakan untuk perencanaan kedatangan pasien)
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result statusAntrian() throws UnsupportedEncodingException {
        ObjectNode response = Json.newObject();
        ObjectNode metadata = Json.newObject();
        ObjectNode result = Json.newObject();

        verifToken();
        if (tokenVerified) {
            JsonNode jsonNode = request().body().asJson();
            String kodepoli = StringUtils.isBlank(jsonNode.get("kodepoli").asText()) ? null : jsonNode.get("kodepoli").asText();
            String tanggalperiksa = StringUtils.isBlank(jsonNode.get("tanggalperiksa").asText()) ? null : jsonNode.get("tanggalperiksa").asText();
            Long kodedokter = Long.parseLong(jsonNode.get("kodedokter").asText());
            String jampraktek = StringUtils.isBlank(jsonNode.get("jampraktek").asText()) ? null : jsonNode.get("jampraktek").asText();

            String[] arrOfJampraktek = jampraktek.split("-");
            String jampraktekbuka = arrOfJampraktek[0];
            String jampraktektutup = arrOfJampraktek[1];

            if (oauth() != null) {
                try {
                    //Pengecekan Format Date [tanggalperiksa]
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar todayCal = Calendar.getInstance();
                    todayCal.setTime(new Date());
                    todayCal.set(Calendar.HOUR_OF_DAY,00);
                    todayCal.set(Calendar.MINUTE,00);
                    todayCal.set(Calendar.SECOND,0);
                    todayCal.set(Calendar.MILLISECOND,0);

                    System.out.println(sdf.parse(tanggalperiksa));
                    System.out.println(todayCal.getTime());

                    if ( sdf.parse(tanggalperiksa).before(todayCal.getTime()) || sdf.parse(tanggalperiksa).after(todayCal.getTime())){
                        metadata.put("message", "Tanggal Periksa Tidak Berlaku").put("code", 201);
                        return ok(metadata);
                    }

                    //Kirim request ke FlexGW
                    ObjectMapper mapper = new ObjectMapper();
                    WSClient ws = Play.current().injector().instanceOf(WSClient.class);
                    CompletionStage<JsonNode> responseComplete = ws.url(getFlexGwBaseUrl() + "/bpjs/polyclinics/schedules")
                            .addHeader("Authorization", "Bearer " + oauth())
                            .addHeader("Content-Type", "application/json")
                            .addHeader("X-Provider-Code", getProvider())
                            .addQueryParameter("startDate", tanggalperiksa).addQueryParameter("endDate", tanggalperiksa).addQueryParameter("polyCode", kodepoli).addQueryParameter("doctorCode", String.valueOf(kodedokter)).addQueryParameter("startTime", jampraktekbuka).addQueryParameter("endTime", jampraktektutup)
                            .get()
                            .thenApply(wsResponse -> wsResponse.asJson());
                    CompletableFuture<JsonNode> node = responseComplete.toCompletableFuture();
                    JsonNode jsonNodes = node.get();
                    Logger.debug(jsonNodes.toString());

                    try {
                        if (jsonNodes.get("code").asText().matches("GWSCH001")) {
                            metadata.put("message", "Daftar Jadwal Dokter Kosong").put("code", 201);
                            return ok(metadata);
                        }
                        if (jsonNodes.get("code").asText().matches("GWSCH004")) {
                            metadata.put("message", "Poli Tidak Ditemukan").put("code", 201);
                            return ok(metadata);
                        }

                    } catch (NullPointerException e) {
                        Map<String, Object> data = mapper.convertValue(jsonNodes, Map.class);
                        Map<String, Object> clinic = mapper.convertValue(jsonNodes.get("clinic"), Map.class);
                        Map<String, Object> polyclinic = mapper.convertValue(clinic.get("polyclinic"), Map.class);
                        Map<String, Object> doctor = mapper.convertValue(jsonNodes.get("doctor"), Map.class);
                        response.put("namapoli", polyclinic.get("name").toString())
                                .put("namadokter", doctor.get("fullName").toString())
                                .put("totalantrean", data.get("bpjsUsedOnlineQuota").toString())
                                .put("sisaantrean", data.get("bpjsUsedOnlineQuota").toString())
                                .put("antreanpanggil", String.valueOf(0))
                                .put("sisakuotajkn", data.get("bpjsOnlineBalance").toString())
                                .put("kuotajkn", data.get("bpjsAllocatedOnlineQuota").toString())
                                .put("sisakuotanonjkn", Long.parseLong(data.get("bpjsOfflineBalance").toString()))
                                .put("kuotanonjkn", Long.parseLong(data.get("bpjsAllocatedOfflineQuota").toString()))
                                .put("keterangan", "");

                        metadata.put("message", "Ok").put("code", 200);
                        result.put("response", response);
                        result.put("metadata", metadata);
                        return ok(result);
                    }

                }catch (ParseException ex){
                    metadata.put("message","Format Tanggal Tidak Sesuai, format yang benar adalah yyyy-mm-dd").put("code",201);
                    return ok(metadata);
                }
                catch (Exception e) {
                    Logger.error(e.getClass().getName() + " / " + e.getMessage());
                    metadata.put("message", "Gagal").put("code", 201);
                    return ok(metadata);
                }
            } else {
                metadata.put("message", "Gagal").put("code", 201);
                return ok(metadata);
            }

        } else {
            metadata.put("message", "Token Expired").put("code", 201);
            return ok(metadata);
        }
        metadata.put("message", "Gagal").put("code", 201);
        return ok(metadata);
    }

    /**
     * Jadwal Operasi RS
     * Fungsi : Informasi jadwal operasi di rumah sakit
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result jadwalOperasi() throws UnsupportedEncodingException {
        ObjectNode response = Json.newObject();
        ObjectNode metadata = Json.newObject();
        ObjectNode result = Json.newObject();

        verifToken();
        if (tokenVerified) {
            JsonNode jsonNode = request().body().asJson();
            String tanggalawal = StringUtils.isBlank(jsonNode.get("tanggalawal").asText()) ? null : jsonNode.get("tanggalawal").asText();
            String tanggalakhir = StringUtils.isBlank(jsonNode.get("tanggalakhir").asText()) ? null : jsonNode.get("tanggalakhir").asText();

            if (oauth() != null) {
                try {

                    if (!validDate(tanggalawal, tanggalakhir)){
                        metadata.put("message", "Tanggal Tidak Valid").put("code", 201);
                        return ok(metadata);
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    WSClient ws = Play.current().injector().instanceOf(WSClient.class);
                    CompletionStage<JsonNode> responseComplete = ws.url(getFlexGwBaseUrl() + "/anci/operationplan/schedules")
                            .addHeader("Authorization", "Bearer " + oauth())
                            .addHeader("Content-Type", "application/json")
                            .addHeader("X-Provider-Code", getProvider())
                            .addHeader("X-Dev-Provider-Code", getProvider())
                            .addQueryParameter("tanggalawal", tanggalawal).addQueryParameter("tanggalakhir", tanggalakhir)
                            .get()
                            .thenApply(wsResponse -> wsResponse.asJson());
                    CompletableFuture<JsonNode> node = responseComplete.toCompletableFuture();
                    JsonNode jsonNodes = node.get();
                    try {
                        if (jsonNodes.get("code").asText().matches("GWOPSCH001")) {
                            metadata.put("message", "Gagal").put("code", 201);
                            return ok(metadata);
                        }
                    } catch (NullPointerException e) {
                        metadata.put("message", "Ok").put("code", 200);
                        result.put("response", jsonNodes);
                        result.put("metadata", metadata);
                        return ok(result);
                    }

                    metadata.put("message", "Ok").put("code", 200);
                    result.put("response", jsonNodes);
                    result.put("metadata", metadata);
                    return ok(result);

                } catch (Exception e) {
                    Logger.error(e.getClass().getName() + "/" + e.getMessage());
                    metadata.put("message", "Gagal").put("code", 201);
                    return ok(metadata);
                }
            } else {
                metadata.put("message", "Gagal").put("code", 201);
                return ok(metadata);
            }

        } else {
            metadata.put("message", "Gagal").put("code", 201);
            return ok(metadata);
        }
    }

    /**
     * Jadwal Operasi RS
     * Fungsi : Informasi jadwal operasi di rumah sakit
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result jadwalOperasiByNoPeserta() throws UnsupportedEncodingException {
        ObjectNode response = Json.newObject();
        ObjectNode metadata = Json.newObject();
        ObjectNode result = Json.newObject();

        verifToken();
        if (tokenVerified) {
            JsonNode jsonNode = request().body().asJson();
            String nopeserta = StringUtils.isBlank(jsonNode.get("nopeserta").asText()) ? null : jsonNode.get("nopeserta").asText();

            if (oauth() != null) {
                try {

                    // Format Nomor Kartu hanya numeric dan 13 digit
                    for (int i = 0; i < nopeserta.length(); i++) {
                        if (nopeserta.charAt(i) >= '0' && nopeserta.charAt(i) <= '9' && nopeserta.length() == 13){

                        }else{
                            metadata.put("message", "Format Nomor Kartu Tidak Sesuai").put("code", 201);
                            return ok(metadata);
                        }
                    }

                    ObjectMapper mapper = new ObjectMapper();
                    WSClient ws = Play.current().injector().instanceOf(WSClient.class);
                    CompletionStage<JsonNode> responseComplete = ws.url(getFlexGwBaseUrl() + "/anci/operationplan/schedules/bpjsnumber")
                            .addHeader("Authorization", "Bearer " + oauth())
                            .addHeader("Content-Type", "application/json")
                            .addHeader("X-Provider-Code", getProvider())
                            .addHeader("X-Dev-Provider-Code", getProvider())
                            .addQueryParameter("nopeserta", nopeserta)
                            .get()
                            .thenApply(wsResponse -> wsResponse.asJson());
                    CompletableFuture<JsonNode> node = responseComplete.toCompletableFuture();
                    JsonNode jsonNodes = node.get();
                    Map<String, Object> data = mapper.convertValue(jsonNodes, Map.class);

                    response.put("kodebooking", data.get("kodebooking").toString())
                            .put("tanggaloperasi", data.get("tanggaloperasi").toString())
                            .put("jenistindakan", data.get("jenistindakan").toString())
                            .put("kodepoli", data.get("kodepoli").toString())
                            .put("namapoli", data.get("namapoli").toString())
                            .put("terlaksana", Long.parseLong(data.get("terlaksana").toString()))
                            .put("nopeserta", data.get("nopeserta").toString())
                            .put("lastupdate", Long.parseLong(data.get("lastupdate").toString()));

                    metadata.put("message", "Ok").put("code", 200);
                    result.put("response", response);
                    result.put("metadata", metadata);
                    return ok(result);

                } catch (Exception e) {
                    Logger.error(e.getClass().getName() + "/" + e.getMessage());
                    metadata.put("message", "Gagal").put("code", 201);
                    return ok(metadata);
                }
            } else {
                metadata.put("message", "Gagal").put("code", 201);
                return ok(metadata);
            }

        } else {
            metadata.put("message", "Gagal").put("code", 201);
            return ok(metadata);
        }
    }

    /**
     * Batal Antrean
     * Fungsi : Membatalkan antrean pasien
     */
    @BodyParser.Of(BodyParser.Json.class)
    public Result batalAntrian() throws UnsupportedEncodingException {
        ObjectNode metadata = Json.newObject();
        ObjectNode result = Json.newObject();

        verifToken();
        if (tokenVerified) {
            JsonNode jsonNode = request().body().asJson();
            String kodebooking = StringUtils.isBlank(jsonNode.get("kodebooking").asText()) ? null : jsonNode.get("kodebooking").asText();
            String keterangan = StringUtils.isBlank(jsonNode.get("keterangan").asText()) ? null : jsonNode.get("keterangan").asText();


            if (oauth() != null) {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    WSClient ws = Play.current().injector().instanceOf(WSClient.class);
                    CompletionStage<JsonNode> responseComplete = ws.url(getFlexGwBaseUrl() + "/appointments/" + kodebooking)
                            .addHeader("Authorization", "Bearer " + oauth())
                            .addHeader("Content-Type", "application/x-www-form-urlencoded")
                            .addHeader("X-Provider-Code", "rs2")
                            .delete()
                            .thenApply(wsResponse -> wsResponse.asJson());
                    CompletableFuture<JsonNode> node = responseComplete.toCompletableFuture();
                    JsonNode jsonNodes = node.get();
                    Map<String, Object> data = mapper.convertValue(jsonNodes, Map.class);
                    System.out.println(data.get("code").toString());

                    if (data.get("code").toString().matches("GWDELETEAPT")) {
                        metadata.put("message", "Ok").put("code", 200);
                    }
                    if (data.get("code").toString().matches("GWAPT000")) {
                        metadata.put("message", "Antrean Tidak Ditemukan").put("code", 201);
                    }

                    if (data.get("code").toString().matches("GWAPT027")) {
                        metadata.put("message", "Pasien Sudah Dilayani, Antrean Tidak Dapat Dibatalkan").put("code", 201);
                    }

                    if (data.get("code").toString().matches("GWAPT028")) {
                        metadata.put("message", "Antrean Tidak Ditemukan atau Sudah Dibatalkan").put("code", 201);
                    }

                    result.put("metadata", metadata);
                    return ok(result);

                } catch (Exception e) {
                    Logger.error(e.getMessage());
                    metadata.put("message", "Gagal").put("code", 201);
                    return ok(metadata);
                }
            } else {
                metadata.put("message", "Gagal").put("code", 201);
                return ok(metadata);
            }

        } else {
            metadata.put("message", "Gagal").put("code", 201);
            return ok(metadata);
        }
    }

    public Result generateSignedToken() throws UnsupportedEncodingException {
        ObjectNode response = Json.newObject();
        ObjectNode metadata = Json.newObject();
        ObjectNode result = Json.newObject();

        String username = config.getString("apis.bpjs.username");
        String password = config.getString("apis.bpjs.password");
        if (request().getHeader("x-username") != null && request().getHeader("x-password") != null) {
            if (request().getHeader("x-username").equals(username) && request().getHeader("x-password").equals(password)) {
                response.put("token", getSignedToken(username, password));
                metadata.put("message", "Ok").put("code", 200);
                result.put("metadata", metadata);
                result.put("response", response);
                return ok(result);
            } else {
                metadata.put("message", "Username atau Password Tidak Sesuai").put("code", 201);
                return ok(metadata);
            }
        } else {
            metadata.put("message", "Gagal").put("code", 201);
            return ok(metadata);
        }

    }

    public Result verifyToken() throws UnsupportedEncodingException {
        ObjectNode metadata = Json.newObject();
        try {
            String username = config.getString("apis.bpjs.username");
            if (request().getHeader("x-username") != null && request().getHeader("x-token") != null) {
                if (request().getHeader("x-username").equals(username)) {
                    DecodedJWT jwt = JWT.decode(request().getHeader("x-token"));
                    tokenVerified = true;
                    try {
                        String secret = config.getString("apis.play.http.secret.key");
                        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                                .withIssuer("auth0")
                                .acceptExpiresAt(5) // accept expiry of 5 minutes
                                .build();
                        verifier.verify(request().getHeader("x-token")).getClaims();
                        metadata.put("message", "OK").put("code", 200);
                        return ok(metadata);

                    } catch (TokenExpiredException e) {
                        tokenExpired = true;
                        metadata.put("message", "Token Expired").put("code", 201);
                        return ok(metadata);
                    }

                }
            }

        } catch (JWTDecodeException exception) {
            //Invalid token
            tokenVerified = false;
            metadata.put("message", "Token Invalid").put("code", 201);
            return ok(metadata);
        }

        metadata.put("message", "Gagal").put("code", 201);
        return ok(metadata);
    }

    private String getSignedToken(String username, String password) throws UnsupportedEncodingException {
        String secret = config.getString("apis.play.http.secret.key");
        Algorithm algorithm = Algorithm.HMAC256(secret);
        String token = JWT.create()
                .withIssuer("auth0")
                .withClaim("user_id", username)
                .withClaim("password", password)
                .withExpiresAt(new Date(System.currentTimeMillis() + (60 * 60 * 1000))) // 5 minutes
                .sign(algorithm);
        return token;


    }

    private void verifToken() throws UnsupportedEncodingException {
        try {
            String username = config.getString("apis.bpjs.username");
            if (request().getHeader("x-username") != null && request().getHeader("x-token") != null) {
                if (request().getHeader("x-username").equals(username)) {
                    DecodedJWT jwt = JWT.decode(request().getHeader("x-token"));
                    try {
                        String secret = config.getString("apis.play.http.secret.key");
                        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                                .withIssuer("auth0")
                                .acceptExpiresAt(60)
                                .build();
                        verifier.verify(request().getHeader("x-token")).getClaims();
                        tokenVerified = true;

                    } catch (TokenExpiredException e) {
                        tokenExpired = true;
                        tokenVerified = false;
                    }

                }
            }

        } catch (JWTDecodeException exception) {
            //Invalid token
            tokenVerified = false;
        }
    }

    public Long scheduleId(ScheduleParams param) throws UnsupportedEncodingException {
        try {
            ObjectMapper mapper = new ObjectMapper();
            WSClient ws = Play.current().injector().instanceOf(WSClient.class);
            CompletionStage<JsonNode> response = ws.url(getFlexGwBaseUrl() + "/bpjs/polyclinics/schedules")
                    .addHeader("Authorization", "Bearer " + oauth())
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-Provider-Code", getProvider())
                    .addQueryParameter("startDate", param.getStartDate()).addQueryParameter("endDate", param.getEndDate()).addQueryParameter("polyCode", param.getPolyCode()).addQueryParameter("doctorCode", param.getDoctorCode()).addQueryParameter("startTime", param.getStartTime()).addQueryParameter("endTime", param.getEndTime())
                    .get()
                    .thenApply(wsResponse -> wsResponse.asJson());
            CompletableFuture<JsonNode> node = response.toCompletableFuture();
            JsonNode jsonNode = node.get();
            Map<String, Object> data = mapper.convertValue(jsonNode, Map.class);
            // response from schedule doctor
            sisakuotajkn = Long.parseLong(data.get("bpjsOnlineBalance").toString());
            kuotajkn = Long.parseLong(data.get("bpjsAllocatedOnlineQuota").toString());
            sisakuotanonjkn = Long.parseLong(data.get("bpjsOfflineBalance").toString());
            kuotanonjkn = Long.parseLong(data.get("bpjsAllocatedOfflineQuota").toString());
            estimasidilayani = Long.parseLong(data.get("date").toString());
            angkaantrean = Long.parseLong(data.get("bpjsUsedOnlineQuota").toString());

            return jsonNode.get("id").asLong();
        } catch (Exception e) {
            return null;
        }
    }

    public String oauth() throws UnsupportedEncodingException {
        try {
            WSClient ws = Play.current().injector().instanceOf(WSClient.class);
            CompletionStage<JsonNode> response = ws.url(getFlexGwTokenUrl() + "/oauth2/token")
                    .setContentType("application/x-www-form-urlencoded")
                    .post("grant_type=client_credentials&scope=read,write&client_id=jknbpjs&client_secret=a2919c46-1fbb-406e-9f14-36910a06c762")
                    .thenApply(wsResponse -> wsResponse.asJson());
            CompletableFuture<JsonNode> node = response.toCompletableFuture();
            JsonNode jsonNode = node.get();
            return jsonNode.get("access_token").asText();

        } catch (Exception e) {
            Logger.error(this.getClass().getName(), e);
            return null;

        }
    }

    public Boolean validBirtDate (String birtdate){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date date = sdf.parse(birtdate);
            String regex = "^\\d{4}-\\d{2}-\\d{2}$";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(birtdate);
            if (date.after(new Date()) || !matcher.matches()){
                return false;
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean validDate (String firstDate, String lastDate){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            Date firstDate_dt = sdf.parse(firstDate);
            Date lastDate_dt = sdf.parse(lastDate);

            if (firstDate_dt.after(lastDate_dt)){
                return false;
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
