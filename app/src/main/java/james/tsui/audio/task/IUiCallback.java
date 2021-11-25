package james.tsui.audio.task;

public interface IUiCallback {
    void onPreExecute();

    void onPostExecute(Long aLong);

    void onError(int code);
}
