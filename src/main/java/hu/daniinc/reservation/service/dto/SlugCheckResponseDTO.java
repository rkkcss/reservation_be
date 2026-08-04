package hu.daniinc.reservation.service.dto;

import java.util.List;
import java.util.Objects;

public class SlugCheckResponseDTO {

    private boolean available;
    private List<String> suggestions;

    public SlugCheckResponseDTO() {}

    public SlugCheckResponseDTO(boolean available, List<String> suggestions) {
        this.available = available;
        this.suggestions = suggestions;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SlugCheckResponseDTO that = (SlugCheckResponseDTO) o;
        return available == that.available && Objects.equals(suggestions, that.suggestions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(available, suggestions);
    }
}
