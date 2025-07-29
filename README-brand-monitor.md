# Brand Digital Presence Monitor - n8n Workflow

A comprehensive n8n workflow that transforms Instagram Reels trend watching into a multi-platform brand digital presence monitoring solution. This workflow provides AI-powered insights, competitive analysis, and automated reporting across major social media platforms.

## 🚀 Features

### Multi-Platform Monitoring
- **Instagram**: Posts, stories, reels, engagement metrics
- **Facebook**: Page posts, engagement, reach analytics
- **Twitter/X**: Tweets, mentions, hashtag tracking
- **LinkedIn**: Company posts, professional engagement
- **TikTok**: Video content, trending analysis
- **YouTube**: Channel videos, shorts, performance metrics

### AI-Powered Analytics
- **Sentiment Analysis**: GPT-4 powered content sentiment evaluation
- **Performance Insights**: Automated performance scoring and trends
- **Competitive Intelligence**: Market positioning and opportunity analysis
- **Content Recommendations**: AI-generated improvement suggestions

### Automated Reporting
- **Comprehensive Reports**: Executive summaries with key metrics
- **Notion Integration**: Structured data storage and visualization
- **Email Alerts**: Performance threshold monitoring and notifications
- **Action Items**: Automated task generation with deadlines

### Advanced Configuration
- **Variable-Based Setup**: Easy customization for different brands
- **Modular Architecture**: Enable/disable features as needed
- **Error Handling**: Robust error management and logging
- **Scheduling**: Flexible timing with cron expressions

## 📋 Quick Start

### Prerequisites
- n8n instance (self-hosted or cloud)
- API access to social media platforms
- OpenAI API key for AI analysis
- Notion workspace for data storage
- SendGrid account for email alerts (optional)

### Installation Steps

1. **Import Workflow**
   ```bash
   # Download the workflow file
   wget https://raw.githubusercontent.com/your-repo/brand-digital-presence-monitor-workflow.json
   
   # Import into n8n via the web interface
   # Go to n8n > Workflows > Import from file
   ```

2. **Configure Credentials**
   Set up the following credentials in n8n:
   - Instagram Basic Display API
   - Facebook Graph API
   - Twitter OAuth2 API
   - LinkedIn OAuth2 API
   - YouTube OAuth2 API
   - OpenAI API
   - Notion API
   - SendGrid API (optional)

3. **Set Up Notion Databases**
   Create two Notion databases using the templates provided:
   - Main Reports Database
   - Execution Logs Database (optional)

4. **Configure Workflow Variables**
   Edit the "Workflow Variables" node with your settings:
   ```json
   {
     "brand_name": "Your Brand Name",
     "monitoring_keywords": "yourbrand,product,campaign",
     "platforms_enabled": "instagram,facebook,twitter,linkedin,youtube",
     "notion_database_id": "your-notion-database-id",
     "days_to_analyze": 7,
     "include_sentiment_analysis": true
   }
   ```

5. **Test and Activate**
   - Test individual nodes to verify API connections
   - Run the complete workflow once manually
   - Activate the scheduled trigger for automation

## 🔧 Configuration Options

### Core Variables
| Variable | Type | Description | Default |
|----------|------|-------------|---------|
| `brand_name` | String | Your brand/company name | "Your Brand Name" |
| `monitoring_keywords` | String | Comma-separated keywords to track | "brand,company,product" |
| `platforms_enabled` | String | Platforms to monitor | "instagram,facebook,twitter,linkedin,tiktok,youtube" |
| `notion_database_id` | String | Notion database ID for reports | Required |
| `days_to_analyze` | Number | Days of data to analyze | 7 |
| `max_posts_per_platform` | Number | Maximum posts per platform | 50 |

### Analysis Settings
| Variable | Type | Description | Default |
|----------|------|-------------|---------|
| `include_sentiment_analysis` | Boolean | Enable AI sentiment analysis | true |
| `include_competitive_analysis` | Boolean | Enable competitive analysis | true |
| `competitive_brands` | String | Competitor names to analyze | "" |
| `sentiment_threshold` | Number | Sentiment alert threshold | 0.1 |

### Notification Settings
| Variable | Type | Description | Default |
|----------|------|-------------|---------|
| `send_email_alerts` | Boolean | Enable email alerts | true |
| `alert_email` | String | Email for alerts | "marketing@yourbrand.com" |
| `performance_threshold` | Number | Performance score alert threshold | 30 |

## 📊 Output Structure

### Main Report Schema
```json
{
  "report_id": "report_1234567890",
  "brand_name": "Your Brand",
  "report_date": "2024-07-29T08:00:00.000Z",
  "executive_summary": {
    "overall_performance_score": 75,
    "total_engagement": 15420,
    "engagement_rate": 3.2,
    "reach": 45000,
    "sentiment_score": 0.85,
    "market_position": "Strong Competitor"
  },
  "platform_performance": {
    "instagram": {
      "engagement_rate": 4.1,
      "performance_score": 82,
      "total_interactions": 5420
    }
  },
  "ai_insights": "Comprehensive AI analysis...",
  "competitive_insights": "Market position analysis...",
  "recommendations": [
    {
      "priority": "High",
      "category": "Engagement",
      "recommendation": "Focus on video content to improve engagement"
    }
  ],
  "action_items": [
    {
      "task": "Review content strategy",
      "deadline": "2024-08-05T00:00:00.000Z",
      "assignee": "Marketing Team"
    }
  ]
}
```

## 🎯 Use Cases

### Brand Managers
- Monitor brand mentions and sentiment across platforms
- Track campaign performance and ROI
- Identify trending topics and opportunities
- Generate executive reports for stakeholders

### Marketing Teams
- Optimize content strategy based on performance data
- Benchmark against competitors
- Identify influencer collaboration opportunities
- Monitor hashtag and keyword performance

### Social Media Managers
- Track engagement rates and growth metrics
- Identify top-performing content types
- Monitor audience sentiment and feedback
- Automate routine reporting tasks

### Business Intelligence
- Integrate social media data with business metrics
- Create comprehensive digital presence dashboards
- Track market share and competitive positioning
- Generate automated insights and recommendations

## 🔒 Security & Privacy

### Data Protection
- All API credentials stored securely in n8n credential store
- No sensitive data logged in plain text
- Configurable data retention policies
- GDPR-compliant data handling

### Access Control
- Role-based access to workflow and reports
- Secure credential management
- Audit logs for all executions
- Environment variable support for sensitive configuration

## 🚨 Monitoring & Alerts

### Performance Monitoring
- Automatic data quality scoring
- API rate limit monitoring
- Error detection and logging
- Execution time tracking

### Alert Conditions
- Performance score below threshold
- Sentiment score drops significantly
- API failures or rate limits
- Data collection issues

### Notification Channels
- Email alerts with detailed reports
- Slack integration (configurable)
- Microsoft Teams webhooks (configurable)
- Notion database notifications

## 📈 Scaling & Optimization

### Performance Tips
- Use incremental data collection for large datasets
- Implement caching for repeated API calls
- Optimize AI prompt length to reduce token usage
- Use batch processing for multiple brands

### Multi-Brand Setup
```json
{
  "brands": [
    {
      "brand_name": "Brand A",
      "monitoring_keywords": "branda,producta",
      "notion_database_id": "database-a-id"
    },
    {
      "brand_name": "Brand B", 
      "monitoring_keywords": "brandb,productb",
      "notion_database_id": "database-b-id"
    }
  ]
}
```

### Cost Optimization
- Monitor OpenAI token usage
- Implement smart data sampling
- Use free tiers where available
- Cache results to reduce API calls

## 🛠️ Troubleshooting

### Common Issues

#### Authentication Errors
```bash
# Check credential expiration
# Verify API permissions
# Test credentials independently
```

#### Data Collection Issues
```bash
# Verify brand names and usernames
# Check account privacy settings
# Review API rate limits
```

#### AI Analysis Failures
```bash
# Check OpenAI API credits
# Verify content policy compliance
# Monitor token usage
```

### Debug Mode
Enable detailed logging by setting `debug_mode: true` in workflow variables.

### Support Resources
- Configuration Guide: `brand-monitor-configuration-guide.md`
- Template Configuration: `brand-monitor-config-template.json`
- API Documentation: Links in configuration template
- Community Forums: n8n Community

## 🤝 Contributing

### Feature Requests
- Additional social media platforms
- Enhanced AI analysis capabilities
- Custom visualization options
- Integration with BI tools

### Bug Reports
Please include:
- n8n version
- Workflow version
- Error logs
- Steps to reproduce

## 📄 License

This workflow is provided for educational and commercial use. Please ensure compliance with all platform APIs' terms of service.

## 🔄 Version History

- **v1.0.0** (2024-07-29): Initial comprehensive brand monitoring workflow
  - Multi-platform data collection
  - AI-powered analysis and insights
  - Notion integration and reporting
  - Error handling and monitoring
  - Flexible configuration system

## 📞 Support

For technical support and questions:
- Check the configuration guide
- Review troubleshooting section
- Test individual workflow nodes
- Verify API credentials and permissions

---

**Built with ❤️ for comprehensive brand digital presence monitoring**