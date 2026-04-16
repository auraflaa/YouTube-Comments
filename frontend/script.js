// Determine API base URL: Use relative path for Nginx proxy (Docker)
// or absolute path for local development (Python server).
const API_BASE_URL = window.location.port === '3000' && window.location.hostname === 'localhost' 
    ? 'http://localhost:8080/api/sentiment' 
    : '/api/sentiment';

let currentData = null; // Global store for results

/**
 * Handle the analyze button click event
 */
async function handleAnalyze(event) {
    event.preventDefault();

    const videoUrl = document.getElementById('videoUrl').value.trim();

    if (!videoUrl) {
        showError('Please enter a valid YouTube video URL');
        return;
    }

    // Hide error and results sections
    document.getElementById('errorSection').style.display = 'none';
    document.getElementById('resultsSection').style.display = 'none';

    // Show loader
    const analyzeBtn = document.getElementById('analyzeBtn');
    const loader = document.getElementById('loader');
    const btnText = document.querySelector('.btn-text');

    analyzeBtn.disabled = true;
    btnText.style.display = 'none';
    loader.style.display = 'inline-block';

    startStatusAnimation();

    console.log('%c 🔍 Starting Discovery...', 'color: #3B82F6; font-weight: bold;');
    console.log('Video URL:', videoUrl);

    try {
        const maxCommentsVal = document.getElementById('maxComments')?.value || 100;
        
        const payload = {
            videoUrl: videoUrl,
            maxComments: parseInt(maxCommentsVal)
        };

        // Make API request
        const response = await fetch(`${API_BASE_URL}/analyze`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.error || 'An error occurred during analysis');
        }

        // Log results to console
        console.log('%c ✅ Analysis Complete!', 'color: #10B981; font-weight: bold;');
        console.log('Total Comments Analyzed:', data.totalComments);
        console.table({
            'Discovery Method': 'Technical Extraction',
            'Sample Size': data.topComments.length,
            'Queue Status': 'Verified'
        });

        // Display results
        currentData = data; 
        displayResults(data);

    } catch (error) {
        console.error('%c ❌ Analysis Failed:', 'color: #EF4444; font-weight: bold;', error);
        showError(error.message || 'Failed to analyze the video. Please check the URL and try again.');
    } finally {
        // Hide loader and restore button
        stopStatusAnimation();
        analyzeBtn.disabled = false;
        btnText.style.display = 'inline-block';
        loader.style.display = 'none';
    }
}

/**
 * Display the analysis results (extracted comments)
 */
function displayResults(data) {
    // Render AI Analytics
    document.getElementById('aiSummary').textContent = data.summary || "No summary captured.";
    
    // Set Toxicity Meter & Status
    const toxicityScore = data.toxicityScore || 0;
    const toxEl = document.getElementById('toxicityScore');
    toxEl.textContent = toxicityScore;
    
    // Clear old status if exists
    const oldStatus = document.querySelector('.toxicity-status');
    if (oldStatus) oldStatus.remove();
    
    // Add dynamic status label
    let statusStr = "Safe";
    let color = "#10B981";
    if (toxicityScore > 20) { statusStr = "Moderate"; color = "#F59E0B"; }
    if (toxicityScore > 50) { statusStr = "Caution"; color = "#EF4444"; }
    
    const statusSpan = document.createElement('span');
    statusSpan.className = 'toxicity-status';
    statusSpan.textContent = statusStr;
    statusSpan.style.color = color;
    statusSpan.style.borderColor = color;
    toxEl.parentElement.appendChild(statusSpan);

    const toxicityFill = document.getElementById('toxicityFill');
    toxicityFill.style.width = '0%'; // Reset for animation
    setTimeout(() => toxicityFill.style.width = toxicityScore + '%', 100);
    
    // Set Sentiment Bars
    const setBar = (idPrefix, val) => {
        const textEl = document.getElementById(`${idPrefix}Val`);
        const fillEl = document.getElementById(`${idPrefix}Fill`);
        if (textEl) textEl.textContent = `${val}%`;
        if (fillEl) {
            fillEl.style.width = '0%';
            setTimeout(() => fillEl.style.width = `${val}%`, 200);
        }
    };
    setBar('pos', data.positivePercentage ? Math.round(data.positivePercentage) : 0);
    setBar('neu', data.neutralPercentage ? Math.round(data.neutralPercentage) : 0);
    setBar('neg', data.negativePercentage ? Math.round(data.negativePercentage) : 0);

    // Render Key Topics
    const topicsContainer = document.getElementById('keyTopicsList') || document.getElementById('ai-topics');
    if (topicsContainer) {
        topicsContainer.innerHTML = '';
        if (data.keyTopics && data.keyTopics.length > 0) {
            data.keyTopics.forEach(topic => {
                const badge = document.createElement('span');
                badge.className = 'topic-badge';
                badge.textContent = topic;
                topicsContainer.appendChild(badge);
            });
        }
    }

    // Render extracted comments
    renderComments(data.topComments);

    // Show sections
    document.getElementById('analytics-section').style.display = 'block';
    document.getElementById('resultsSection').style.display = 'block';
    document.getElementById('errorSection').style.display = 'none';

    // Scroll to results
    setTimeout(() => {
        document.getElementById('resultsSection').scrollIntoView({ behavior: 'smooth' });
    }, 100);
}

/**
 * Filter comments by search query
 */
function handleSearch() {
    const query = document.getElementById('commentSearch').value.toLowerCase();
    if (!currentData || !currentData.topComments) return;
    
    const filtered = currentData.topComments.filter(c => c.toLowerCase().includes(query));
    renderComments(filtered);
}

/**
 * Copy AI Summary to clipboard
 */
function copySummary() {
    const text = document.getElementById('aiSummary').textContent;
    if (!text || text === "Analyzing...") return;
    
    navigator.clipboard.writeText(text).then(() => {
        showToast('Summary copied to clipboard!', '📋');
    }).catch(err => {
        console.error('Failed to copy: ', err);
    });
}

/**
 * Export analysis as JSON
 */
function exportAnalysis() {
    if (!currentData) return;
    try {
        const blob = new Blob([JSON.stringify(currentData, null, 2)], { type: 'application/json' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `youtube_analysis_${Date.now()}.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        showToast('Analysis exported successfully!', '📥');
    } catch (err) {
        showToast('Export failed', '❌');
    }
}

/**
 * Show a premium toast notification
 */
function showToast(message, icon = '✅') {
    let toast = document.getElementById('toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'toast';
        toast.className = 'toast';
        document.body.appendChild(toast);
    }
    
    toast.innerHTML = `<span class="toast-icon">${icon}</span> <span class="toast-message">${message}</span>`;
    toast.classList.add('show');
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

/**
 * Render the extracted comments grid
 */
function renderComments(comments) {
    const grid = document.getElementById('commentsGrid');
    
    // Safety check if grid exists
    if (!grid) return;
    
    grid.innerHTML = ''; // Clear previous

    if (!comments || comments.length === 0) {
        grid.innerHTML = '<p style="color: var(--text-muted); grid-column: 1/-1;">No comments available to display.</p>';
        return;
    }

    comments.forEach((commentText, index) => {
        const card = document.createElement('div');
        card.className = 'comment-card';
        
        const badge = document.createElement('div');
        badge.className = 'comment-badge';
        badge.textContent = `Comment #${index + 1}`;
        
        const text = document.createElement('p');
        text.textContent = commentText;
        text.title = commentText; // Show full text on hover
        
        card.appendChild(badge);
        card.appendChild(text);
        grid.appendChild(card);
    });
}




/**
 * Show error message
 */
function showError(message) {
    document.getElementById('errorMessage').textContent = message;
    document.getElementById('errorSection').style.display = 'block';
    document.getElementById('resultsSection').style.display = 'none';

    setTimeout(() => {
        document.getElementById('errorSection').scrollIntoView({ behavior: 'smooth' });
    }, 100);
}

/**
 * Reset the form
 */
function resetForm() {
    document.getElementById('analysisForm').reset();
    document.getElementById('resultsSection').style.display = 'none';
    document.getElementById('errorSection').style.display = 'none';

    // Scroll to top
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

/**
 * Dynamic Status Message Logic
 */
let statusInterval = null;
const statusMessages = [
    "Initializing Extraction...",
    "Connecting to YouTube API...",
    "Searching channel metadata...",
    "Fetching comment threads...",
    "Parsing raw text...",
    "Sending to Gemma-4-31b for Insights...",
    "Finalizing discovery...",
    "Almost there..."
];

function startStatusAnimation() {
    const statusEl = document.getElementById('loadingStatus');
    if (!statusEl) return;
    
    statusEl.style.display = 'block';
    let index = 0;
    statusEl.textContent = statusMessages[index];
    
    statusInterval = setInterval(() => {
        index++;
        if (index >= statusMessages.length) {
            clearInterval(statusInterval);
            return;
        }
        statusEl.textContent = statusMessages[index];
    }, 1500);
}

function stopStatusAnimation() {
    const statusEl = document.getElementById('loadingStatus');
    if (statusEl) statusEl.style.display = 'none';
    if (statusInterval) clearInterval(statusInterval);
}

/**
 * Reset error display
 */
function resetError() {
    document.getElementById('errorSection').style.display = 'none';
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('%c 🔍 YouTube Comment Discovery', 'font-size: 16px; font-weight: bold; color: #3B82F6;');
    console.log('%c High-Speed Comment Extraction Pipeline', 'font-size: 12px; color: #666;');
    console.log('%c Status:', 'font-weight: bold; color: #333;');
    console.log('✓ UI Discovery Mode Active');
    console.log('✓ API Polling Ready');
    console.log('API Base URL:', API_BASE_URL);
});
