# Yapay sinir ağı ile belirtilerden hastalık tahmini uygulaması

Günümüzde olası hastalıklarının semptomları için internetten araştırma yapmak hasta adayları için yaygın bir alışkanlıktır. Ancak bu hastalıklar için yaptıkları ön araştırma yanıltıcı olabilmektedir.Çoğu	semptom yaygın olarak bilinen hastalıklara yorulabilmekte,bazı semptomlar ise daha az bilinmektedir.Bu yüzden hastalar tarafından  edilinen önyargıların kendileri için hayati sonuçları da olabilmektedir.Bu sorunla baş edebilmek için gerçek hastalardan toplanan veriler ile eğitilmiş bir yapay sinir ağı modeli üzerinde tahminler yapan bir mobil uygulama, kullanıcıları için hayat kurtarıcı olabilir. 

Bu uygulamayı yapabilmek için hastalardan toplanan veriler ile ilgili hastalıkları eşleştiren verilere ihtiyaç vardır.Bu veriler Kaggle gibi ücretsiz veriseti sağlayan platformlardan edinilebilir.
Ayrıca bu verilerin yapay sinir ağına uygun olması gerekmektedir .Çok katmanlı yapay sinir ağları, özellikleri bir veya birden fazla etiketle eşleştirebilmektedir.Kullacağımız verisetleri bu özellikleri taşımaktadır.

Yapay sinir ağlarını eğitmek için en kapsamlı kütüphaneleri sağlayan programlama dillerinden biride Python’dur.Pythonda yapay sinir ağlarınının önişlemesi ve eğitimi için kullanılan en bilindik kütüphaneler Tensorflow,Numpy,Pandas,Scikit-learn’dir.Bu kütüphaneler kullanılarak verisetlerine uygun modeller oluşturulacak.Yine mobil uygulamada kullanılmak üzere bu kütüphanelerden faydalanılarak modeller bir dosyaya aktarılacaktır.

Android mobil işletim sistemi üzerinde çalışan uygulamalar yapmak için Android Studio ve Kotlin programlama dilini kullanmak yaygındır.Ayrıca Kotlin dilinin Tensorflow modelleri üzerinde tahmin yapmak ve yeniden eğitmek için hazır kütüphaneleri olduğu için bu yöntemleri kullanmak uygulama için çok faydalıdır.Android üzerinde çalışan bir mobil uygulama hastalık tahmini için en kolay ulaşılabilir ve kullanılabilir yoldur.

Uygulamamızı yapmak için izlenen yol öncelikle Kaggle sitesinden semptomlar üzerinden herhangi bir hastalığı tahmin eden bir büyük veriseti ,bilinen semptomlar üzerinden kalp hastalığını tahmin eden bir veriseti ve yine diyabetide aynı şekilde tahmin eden bir veriseti bulmak ve bu verisetleri için tensorflow kütüphanesi üzerinde modeller oluşturup eğitmek olmuştur.

Bu modeller beklenebilir oranda tahmin yapmak için yeterli sayıda veri içermeyeceğinden hastaların geri bildirim yapması ve bu geribildirimlerin uygulama üzerinde modellerin yeniden eğitilmesi için kullanılması gerekmektedir.Bu da yine Kotlin dilinde Tensorflow Lite için sağlanan kütüphaneler kullanılarak ve veritabanı içinde Supabase denilen ücretsiz bir Postgre SQL veritabanı kullanarak yapılmıştır.

Ödeve konu olan uygulamanın benzerini Türkiye bölgesinde ve Türkçe dilinde sadece Sağlık Bakanlığı geliştirmiştir.Bu uygulama fikrinin bu kadar az uygulanmış olması bu ödevin sağlayacağı faydayı artıracaktır. 

    

# Genel model için yapılmış üç tahmin
![resim](https://github.com/user-attachments/assets/20e0b43e-c0a3-4456-977f-740f3cf6f5e2)
![resim](https://github.com/user-attachments/assets/93d6d2aa-5366-4d99-8e4b-e9e3e3813750)
![resim](https://github.com/user-attachments/assets/21b6179e-466b-4d4e-bde9-f7bdbbf5ef06)


# Kalp modeli için yapılmış üç tahmin

![resim](https://github.com/user-attachments/assets/692af9e2-ec7a-4d2d-a6c2-7a592f1c3f1c)
![resim](https://github.com/user-attachments/assets/0b3c3922-f5f5-44fd-8e3c-68245f8c245b)
![resim](https://github.com/user-attachments/assets/66d85d52-0ca9-4488-accd-4a50d4f5d02b)


# Diyabet modeli için yapılmış üç tahmin
![resim](https://github.com/user-attachments/assets/f50b4aea-02d8-4964-8266-2bab2079154a)
![resim](https://github.com/user-attachments/assets/5d5bbd71-4a56-45d8-9575-bb61f6bf9d2b)
![resim](https://github.com/user-attachments/assets/066c5539-cacd-4003-bc11-23a5866e613a)
